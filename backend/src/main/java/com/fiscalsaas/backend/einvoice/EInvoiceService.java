package com.fiscalsaas.backend.einvoice;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fiscalsaas.backend.api.ApiConflictException;
import com.fiscalsaas.backend.api.ApiValidationException;
import com.fiscalsaas.backend.api.ResourceNotFoundException;
import com.fiscalsaas.backend.identity.FiscalRole;
import com.fiscalsaas.backend.identity.Membership;
import com.fiscalsaas.backend.identity.TenantAccessDeniedException;
import com.fiscalsaas.backend.identity.TenantAccessService;
import com.fiscalsaas.backend.invoices.FiscalInvoice;
import com.fiscalsaas.backend.invoices.FiscalInvoiceLine;
import com.fiscalsaas.backend.invoices.FiscalInvoiceLineRepository;
import com.fiscalsaas.backend.invoices.FiscalInvoiceRepository;
import com.fiscalsaas.backend.invoices.FiscalInvoiceTax;
import com.fiscalsaas.backend.invoices.FiscalInvoiceTaxRepository;
import com.fiscalsaas.backend.invoices.InvoiceStatus;
import com.fiscalsaas.backend.invoices.InvoiceType;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EInvoiceService {

	private static final EnumSet<FiscalRole> WRITE_ROLES = EnumSet.of(
			FiscalRole.PLATFORM_ADMIN,
			FiscalRole.TENANT_ADMIN,
			FiscalRole.FISCAL_MANAGER,
			FiscalRole.ACCOUNTANT);

	private final TenantAccessService tenantAccess;
	private final FiscalInvoiceRepository invoices;
	private final FiscalInvoiceLineRepository lines;
	private final FiscalInvoiceTaxRepository taxes;
	private final EInvoiceMessageRepository messages;
	private final EInvoiceEventRepository events;
	private final EInvoicePaymentEventRepository paymentEvents;
	private final Map<EInvoiceSyntax, EInvoiceAdapter> adapters;

	EInvoiceService(
			TenantAccessService tenantAccess,
			FiscalInvoiceRepository invoices,
			FiscalInvoiceLineRepository lines,
			FiscalInvoiceTaxRepository taxes,
			EInvoiceMessageRepository messages,
			EInvoiceEventRepository events,
			EInvoicePaymentEventRepository paymentEvents,
			List<EInvoiceAdapter> adapters) {
		this.tenantAccess = tenantAccess;
		this.invoices = invoices;
		this.lines = lines;
		this.taxes = taxes;
		this.messages = messages;
		this.events = events;
		this.paymentEvents = paymentEvents;
		this.adapters = adapters.stream().collect(Collectors.toMap(EInvoiceAdapter::syntax, Function.identity(), (left, right) -> left, () -> new EnumMap<>(EInvoiceSyntax.class)));
	}

	@Transactional(readOnly = true)
	public List<EInvoiceMessageResponse> listMessages(String tenantId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		return messages.findByTenant_IdOrderByCreatedAtDesc(tenantId)
				.stream()
				.map(EInvoiceMessageResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public EInvoiceMessageResponse getMessage(String tenantId, String messageId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		return EInvoiceMessageResponse.from(requireMessage(tenantId, messageId));
	}

	@Transactional
	public EInvoiceMessageResponse createMessage(String tenantId, CreateEInvoiceRequest body, HttpServletRequest request) {
		Membership membership = requireWriteAccess(tenantId, request);
		FiscalInvoice invoice = invoices.findByIdAndTenant_Id(body.invoiceId(), tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Fiscal invoice was not found in the tenant."));
		if (!InvoiceStatus.ISSUED.name().equals(invoice.status())) {
			throw new ApiValidationException("Only issued invoices can generate B2B e-invoice messages.");
		}
		if (messages.existsByTenant_IdAndInvoice_Id(tenantId, invoice.id())) {
			throw new ApiConflictException("The invoice already has a B2B e-invoice message.");
		}
		EInvoiceSyntax syntax = EInvoiceSyntax.fromValue(body.syntax(), EInvoiceSyntax.UBL);
		EInvoiceAdapter adapter = adapters.get(syntax);
		if (adapter == null) {
			throw new ApiValidationException("No e-invoice adapter is configured for the requested syntax.");
		}
		List<FiscalInvoiceLine> invoiceLines = lines.findByInvoice_IdOrderByLineNumberAsc(invoice.id());
		List<FiscalInvoiceTax> invoiceTaxes = taxes.findByInvoice_IdOrderByTaxRateAsc(invoice.id());
		String payload = adapter.render(invoice, invoiceLines, invoiceTaxes);
		Instant now = Instant.now();
		EInvoiceMessage message = messages.save(EInvoiceMessage.create(
				membership.tenant(),
				invoice,
				syntax,
				directionFor(invoice),
				payload,
				sha256(payload),
				tenantAccess.currentUser().id(),
				now));
		events.save(EInvoiceEvent.create(
				membership.tenant(),
				message,
				EInvoiceEventType.MESSAGE_GENERATED,
				"Generated %s payload for invoice %s. Official interoperability is not asserted.".formatted(syntax.name(), invoice.invoiceNumber()),
				tenantAccess.currentUser().id(),
				now));
		return EInvoiceMessageResponse.from(message);
	}

	@Transactional
	public EInvoiceMessageResponse updateStatus(String tenantId, String messageId, UpdateEInvoiceStatusRequest body, HttpServletRequest request) {
		Membership membership = requireWriteAccess(tenantId, request);
		EInvoiceMessage message = requireMessage(tenantId, messageId);
		boolean changed = false;
		if (body.exchangeStatus() != null && !body.exchangeStatus().isBlank()) {
			EInvoiceExchangeStatus exchangeStatus = EInvoiceExchangeStatus.fromValue(body.exchangeStatus());
			message.updateExchangeStatus(exchangeStatus);
			events.save(EInvoiceEvent.create(
					membership.tenant(),
					message,
					EInvoiceEventType.EXCHANGE_STATUS_CHANGED,
					"Exchange status changed to %s".formatted(exchangeStatus.name()),
					tenantAccess.currentUser().id(),
					Instant.now()));
			changed = true;
		}
		if (body.commercialStatus() != null && !body.commercialStatus().isBlank()) {
			EInvoiceCommercialStatus commercialStatus = EInvoiceCommercialStatus.fromValue(body.commercialStatus());
			message.updateCommercialStatus(commercialStatus, body.reason());
			events.save(EInvoiceEvent.create(
					membership.tenant(),
					message,
					EInvoiceEventType.COMMERCIAL_STATUS_CHANGED,
					"Commercial status changed to %s".formatted(commercialStatus.name()),
					tenantAccess.currentUser().id(),
					Instant.now()));
			changed = true;
		}
		if (!changed) {
			throw new ApiValidationException("At least one e-invoice status must be provided.");
		}
		return EInvoiceMessageResponse.from(messages.save(message));
	}

	@Transactional
	public EInvoicePaymentEventResponse createPaymentEvent(
			String tenantId,
			String messageId,
			CreateEInvoicePaymentEventRequest body,
			HttpServletRequest request) {
		Membership membership = requireWriteAccess(tenantId, request);
		EInvoiceMessage message = requireMessage(tenantId, messageId);
		EInvoicePaymentStatus paymentStatus = EInvoicePaymentStatus.fromValue(body.paymentStatus());
		if (paymentStatus == EInvoicePaymentStatus.UNPAID) {
			throw new ApiValidationException("Payment events must mark partial or full payment.");
		}
		Instant now = Instant.now();
		EInvoicePaymentEvent event = paymentEvents.save(EInvoicePaymentEvent.create(
				membership.tenant(),
				message,
				paymentStatus,
				body.amount(),
				body.paidAt() == null ? now : body.paidAt(),
				body.reference(),
				body.notes(),
				tenantAccess.currentUser().id(),
				now));
		message.updatePaymentStatus(paymentStatus);
		messages.save(message);
		events.save(EInvoiceEvent.create(
				membership.tenant(),
				message,
				EInvoiceEventType.PAYMENT_STATUS_CHANGED,
				"Payment status changed to %s for amount %s".formatted(paymentStatus.name(), body.amount().toPlainString()),
				tenantAccess.currentUser().id(),
				now));
		return EInvoicePaymentEventResponse.from(event);
	}

	@Transactional(readOnly = true)
	public String payload(String tenantId, String messageId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		return requireMessage(tenantId, messageId).payload();
	}

	@Transactional(readOnly = true)
	public List<EInvoiceEventResponse> listEvents(String tenantId, String messageId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		requireMessage(tenantId, messageId);
		return events.findByTenant_IdAndMessage_IdOrderByEventAtDesc(tenantId, messageId)
				.stream()
				.map(EInvoiceEventResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<EInvoicePaymentEventResponse> listPaymentEvents(String tenantId, String messageId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		requireMessage(tenantId, messageId);
		return paymentEvents.findByTenant_IdAndMessage_IdOrderByCreatedAtDesc(tenantId, messageId)
				.stream()
				.map(EInvoicePaymentEventResponse::from)
				.toList();
	}

	private EInvoiceDirection directionFor(FiscalInvoice invoice) {
		return InvoiceType.RECEIVED.name().equals(invoice.invoiceType()) ? EInvoiceDirection.INBOUND : EInvoiceDirection.OUTBOUND;
	}

	private Membership requireWriteAccess(String tenantId, HttpServletRequest request) {
		Membership membership = tenantAccess.requireTenantAccess(tenantId, request);
		if (!WRITE_ROLES.contains(membership.fiscalRole())) {
			throw new TenantAccessDeniedException("The current role cannot mutate B2B e-invoice evidence.");
		}
		return membership;
	}

	private EInvoiceMessage requireMessage(String tenantId, String messageId) {
		return messages.findByIdAndTenant_Id(messageId, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("B2B e-invoice message was not found in the tenant."));
	}

	private String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available.", exception);
		}
	}
}

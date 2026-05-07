package com.fiscalsaas.backend.invoices;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiscalsaas.backend.api.ApiConflictException;
import com.fiscalsaas.backend.api.ApiValidationException;
import com.fiscalsaas.backend.api.ResourceNotFoundException;
import com.fiscalsaas.backend.fiscal.AuditEventService;
import com.fiscalsaas.backend.fiscal.CompanyFiscalSettings;
import com.fiscalsaas.backend.fiscal.CompanyFiscalSettingsRepository;
import com.fiscalsaas.backend.fiscal.Customer;
import com.fiscalsaas.backend.fiscal.CustomerRepository;
import com.fiscalsaas.backend.fiscal.InvoiceSeries;
import com.fiscalsaas.backend.fiscal.InvoiceSeriesRepository;
import com.fiscalsaas.backend.identity.Company;
import com.fiscalsaas.backend.identity.CompanyRepository;
import com.fiscalsaas.backend.identity.FiscalRole;
import com.fiscalsaas.backend.identity.Membership;
import com.fiscalsaas.backend.identity.TenantAccessDeniedException;
import com.fiscalsaas.backend.identity.TenantAccessService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceService {

	private static final Set<FiscalRole> WRITE_ROLES = EnumSet.of(
			FiscalRole.PLATFORM_ADMIN,
			FiscalRole.TENANT_ADMIN,
			FiscalRole.FISCAL_MANAGER,
			FiscalRole.ACCOUNTANT);
	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

	private final TenantAccessService tenantAccess;
	private final CompanyRepository companies;
	private final FiscalInvoiceRepository invoices;
	private final FiscalInvoiceLineRepository lines;
	private final FiscalInvoiceTaxRepository taxes;
	private final CompanyFiscalSettingsRepository fiscalSettings;
	private final InvoiceSeriesRepository seriesRepository;
	private final CustomerRepository customers;
	private final InvoicePaymentRepository payments;
	private final InvoiceArtifactRepository artifacts;
	private final InvoicePdfService pdfService;
	private final AuditEventService auditEvents;
	private final ObjectMapper objectMapper = new ObjectMapper();

	InvoiceService(
			TenantAccessService tenantAccess,
			CompanyRepository companies,
			FiscalInvoiceRepository invoices,
			FiscalInvoiceLineRepository lines,
			FiscalInvoiceTaxRepository taxes,
			CompanyFiscalSettingsRepository fiscalSettings,
			InvoiceSeriesRepository seriesRepository,
			CustomerRepository customers,
			InvoicePaymentRepository payments,
			InvoiceArtifactRepository artifacts,
			InvoicePdfService pdfService,
			AuditEventService auditEvents) {
		this.tenantAccess = tenantAccess;
		this.companies = companies;
		this.invoices = invoices;
		this.lines = lines;
		this.taxes = taxes;
		this.fiscalSettings = fiscalSettings;
		this.seriesRepository = seriesRepository;
		this.customers = customers;
		this.payments = payments;
		this.artifacts = artifacts;
		this.pdfService = pdfService;
		this.auditEvents = auditEvents;
	}

	@Transactional(readOnly = true)
	public List<InvoiceResponse> listInvoices(String tenantId, String companyId, String status, String search, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		String normalizedCompanyId = normalizeText(companyId);
		if (normalizedCompanyId != null) {
			requireCompany(tenantId, normalizedCompanyId);
		}
		String normalizedStatus = normalizeStatus(status);
		String normalizedSearch = normalizeText(search);
		return (normalizedCompanyId == null && normalizedStatus == null && normalizedSearch == null
				? invoices.findByTenant_IdOrderByIssueDateDescInvoiceNumberDesc(tenantId)
				: invoices.searchByTenant(tenantId, normalizedCompanyId, normalizedStatus, normalizedSearch))
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public InvoiceResponse getInvoice(String tenantId, String invoiceId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		return toResponse(requireInvoice(tenantId, invoiceId));
	}

	@Transactional
	public InvoiceResponse createInvoice(String tenantId, CreateInvoiceRequest body, HttpServletRequest request) {
		Membership membership = requireWriteAccess(tenantId, request);
		if (invoices.existsByTenant_IdAndInvoiceNumber(tenantId, body.invoiceNumber().trim())) {
			throw new ApiConflictException("An invoice with this number already exists in the tenant.");
		}
		InvoiceType invoiceType = InvoiceType.fromValue(body.invoiceType());
		Company issuer = requireCompany(tenantId, body.issuerCompanyId());
		Company customerCompany = requireCompany(tenantId, body.customerCompanyId());
		Customer customer = resolveCustomer(tenantId, body.customerId());
		FiscalInvoice rectifies = body.rectifiesInvoiceId() == null || body.rectifiesInvoiceId().isBlank()
				? null
				: requireInvoice(tenantId, body.rectifiesInvoiceId());
		if (invoiceType.corrective() && rectifies == null) {
			throw new ApiValidationException("rectifiesInvoiceId is required for corrective invoices.");
		}

		LocalDate issueDate = body.issueDate() == null ? LocalDate.now() : body.issueDate();
		Calculation calculation = calculate(body.lines());
		FiscalInvoice invoice = invoices.save(FiscalInvoice.create(
				membership.tenant(),
				issuer,
				customerCompany,
				customer,
				body.invoiceNumber(),
				invoiceType,
				issueDate,
				resolveDueDate(tenantId, issuer.id(), issueDate, body.dueDate()),
				body.currency() == null || body.currency().isBlank() ? "EUR" : body.currency(),
				calculation.taxableBase(),
				calculation.taxTotal(),
				calculation.total(),
				calculation.withholdingTotal(),
				calculation.grossTotal(),
				calculation.netTotal(),
				rectifies,
				tenantAccess.currentUser().id()));
		storeCalculatedDetails(invoice, calculation);
		auditEvents.record(tenantId, issuer.id(), "INVOICE_DRAFT_CREATED", "INVOICE", invoice.id(), invoice.invoiceNumber());
		return toResponse(invoice);
	}

	@Transactional
	public InvoiceResponse updateInvoice(String tenantId, String invoiceId, CreateInvoiceRequest body, HttpServletRequest request) {
		requireWriteAccess(tenantId, request);
		FiscalInvoice invoice = requireInvoice(tenantId, invoiceId);
		if (!InvoiceStatus.DRAFT.name().equals(invoice.status())) {
			throw new ApiValidationException("Only DRAFT invoices can be edited.");
		}
		String invoiceNumber = body.invoiceNumber().trim();
		if (invoices.existsByTenant_IdAndInvoiceNumberAndIdNot(tenantId, invoiceNumber, invoiceId)) {
			throw new ApiConflictException("An invoice with this number already exists in the tenant.");
		}
		InvoiceType invoiceType = InvoiceType.fromValue(body.invoiceType());
		Company issuer = requireCompany(tenantId, body.issuerCompanyId());
		Company customerCompany = requireCompany(tenantId, body.customerCompanyId());
		Customer customer = resolveCustomer(tenantId, body.customerId());
		FiscalInvoice rectifies = body.rectifiesInvoiceId() == null || body.rectifiesInvoiceId().isBlank()
				? null
				: requireInvoice(tenantId, body.rectifiesInvoiceId());
		if (invoiceType.corrective() && rectifies == null) {
			throw new ApiValidationException("rectifiesInvoiceId is required for corrective invoices.");
		}
		LocalDate issueDate = body.issueDate() == null ? LocalDate.now() : body.issueDate();
		Calculation calculation = calculate(body.lines());
		invoice.updateDraft(
				issuer,
				customerCompany,
				customer,
				invoiceNumber,
				invoiceType,
				issueDate,
				resolveDueDate(tenantId, issuer.id(), issueDate, body.dueDate()),
				body.currency() == null || body.currency().isBlank() ? "EUR" : body.currency(),
				calculation.taxableBase(),
				calculation.taxTotal(),
				calculation.total(),
				calculation.withholdingTotal(),
				calculation.grossTotal(),
				calculation.netTotal(),
				rectifies);
		taxes.deleteByInvoice_Id(invoice.id());
		lines.deleteByInvoice_Id(invoice.id());
		taxes.flush();
		lines.flush();
		storeCalculatedDetails(invoice, calculation);
		auditEvents.record(tenantId, issuer.id(), "INVOICE_DRAFT_UPDATED", "INVOICE", invoice.id(), invoice.invoiceNumber());
		return toResponse(invoice);
	}

	@Transactional
	public InvoiceResponse updateStatus(String tenantId, String invoiceId, UpdateInvoiceStatusRequest body, HttpServletRequest request) {
		InvoiceStatus target = InvoiceStatus.fromValue(body.status());
		if (target == InvoiceStatus.ISSUED) {
			return issueInvoice(tenantId, invoiceId, new IssueInvoiceRequest(null, null), request);
		}
		if (target == InvoiceStatus.CANCELLED || target == InvoiceStatus.CANCELLED_LOCAL) {
			return cancelLocal(tenantId, invoiceId, new CancelInvoiceRequest("Local cancellation requested"), request);
		}
		requireWriteAccess(tenantId, request);
		FiscalInvoice invoice = requireInvoice(tenantId, invoiceId);
		invoice.updateStatus(target);
		auditEvents.record(tenantId, invoice.issuerCompany().id(), "INVOICE_STATUS_UPDATED", "INVOICE", invoice.id(), target.name());
		return toResponse(invoice);
	}

	@Transactional
	public InvoiceResponse issueInvoice(String tenantId, String invoiceId, IssueInvoiceRequest body, HttpServletRequest request) {
		requireWriteAccess(tenantId, request);
		FiscalInvoice invoice = requireInvoice(tenantId, invoiceId);
		if (!InvoiceStatus.DRAFT.name().equals(invoice.status())) {
			throw new ApiValidationException("Only DRAFT invoices can be issued.");
		}
		CompanyFiscalSettings settings = fiscalSettings.findByCompany_IdAndTenant_Id(invoice.issuerCompany().id(), tenantId)
				.orElseThrow(() -> new ApiValidationException("Fiscal settings are required before issuing invoices."));
		requireFiscalSettingsReady(settings);
		if (invoice.customer() != null && !"ACTIVE".equals(invoice.customer().status())) {
			throw new ApiValidationException("Inactive customers cannot be used for issuing invoices.");
		}
		String issueRequestId = body == null || body.issueRequestId() == null || body.issueRequestId().isBlank()
				? UUID.randomUUID().toString()
				: body.issueRequestId().trim();
		if (invoices.existsByTenant_IdAndIssueRequestId(tenantId, issueRequestId)) {
			throw new ApiConflictException("Issue request id was already used for this tenant.");
		}
		InvoiceSeries series = resolveSeriesForIssue(tenantId, invoice.issuerCompany().id(), body == null ? null : body.seriesId());
		String fiscalNumber = series.reserveNextFiscalNumber();
		Instant issuedAt = Instant.now();
		invoice.issue(
				series,
				fiscalNumber,
				issueRequestId,
				writeJson(issuerSnapshot(settings)),
				writeJson(customerSnapshot(invoice)),
				writeJson(totalsSnapshot(invoice)),
				issuedAt);
		auditEvents.record(tenantId, invoice.issuerCompany().id(), "INVOICE_ISSUED", "INVOICE", invoice.id(), fiscalNumber);
		return toResponse(invoice);
	}

	@Transactional
	public InvoiceResponse cancelLocal(String tenantId, String invoiceId, CancelInvoiceRequest body, HttpServletRequest request) {
		requireWriteAccess(tenantId, request);
		FiscalInvoice invoice = requireInvoice(tenantId, invoiceId);
		if (!InvoiceStatus.ISSUED.name().equals(invoice.status())) {
			throw new ApiValidationException("Only ISSUED invoices can be cancelled locally.");
		}
		String reason = body == null || body.reason() == null || body.reason().isBlank()
				? "Local cancellation requested"
				: body.reason().trim();
		invoice.cancelLocal(reason);
		auditEvents.record(tenantId, invoice.issuerCompany().id(), "INVOICE_CANCELLED_LOCAL", "INVOICE", invoice.id(), reason);
		return toResponse(invoice);
	}

	@Transactional
	public InvoiceResponse createCorrective(String tenantId, String invoiceId, HttpServletRequest request) {
		Membership membership = requireWriteAccess(tenantId, request);
		FiscalInvoice original = requireInvoice(tenantId, invoiceId);
		if (!InvoiceStatus.ISSUED.name().equals(original.status()) && !InvoiceStatus.CANCELLED_LOCAL.name().equals(original.status())) {
			throw new ApiValidationException("Only issued or locally cancelled invoices can create corrective drafts.");
		}
		String draftNumber = trimToMax(original.invoiceNumber() + "-RECT-" + System.currentTimeMillis(), 80);
		if (invoices.existsByTenant_IdAndInvoiceNumber(tenantId, draftNumber)) {
			draftNumber = trimToMax(draftNumber + "-" + UUID.randomUUID().toString().substring(0, 8), 80);
		}
		BigDecimal base = original.taxableBase().negate();
		BigDecimal tax = original.taxTotal().negate();
		BigDecimal withholding = original.withholdingTotal().negate();
		BigDecimal total = original.total().negate();
		FiscalInvoice corrective = invoices.save(FiscalInvoice.create(
				membership.tenant(),
				original.issuerCompany(),
				original.customerCompany(),
				original.customer(),
				draftNumber,
				InvoiceType.CORRECTIVE,
				LocalDate.now(),
				original.dueDate(),
				original.currency(),
				base,
				tax,
				total,
				withholding,
				original.grossTotal().negate(),
				original.netTotal().negate(),
				original,
				tenantAccess.currentUser().id()));
		lines.save(FiscalInvoiceLine.create(
				corrective,
				1,
				"Rectificativa local de " + nullToDash(original.fiscalNumber()),
				BigDecimal.ONE,
				original.taxableBase().negate(),
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				"CORRECTIVE",
				base,
				tax,
				total));
		taxes.save(FiscalInvoiceTax.create(corrective, BigDecimal.ZERO.setScale(2), base, tax));
		original.updateStatus(InvoiceStatus.RECTIFIED);
		auditEvents.record(tenantId, original.issuerCompany().id(), "INVOICE_CORRECTIVE_CREATED", "INVOICE", corrective.id(), original.id());
		return toResponse(corrective);
	}

	@Transactional(readOnly = true)
	public List<InvoicePaymentResponse> listPayments(String tenantId, String invoiceId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		requireInvoice(tenantId, invoiceId);
		return payments.findByTenant_IdAndInvoice_IdOrderByPaymentDateDescCreatedAtDesc(tenantId, invoiceId)
				.stream()
				.map(InvoicePaymentResponse::from)
				.toList();
	}

	@Transactional
	public InvoicePaymentResponse addPayment(String tenantId, String invoiceId, CreatePaymentRequest body, HttpServletRequest request) {
		Membership membership = requireWriteAccess(tenantId, request);
		FiscalInvoice invoice = requireInvoice(tenantId, invoiceId);
		if (!InvoiceStatus.ISSUED.name().equals(invoice.status()) && !InvoiceStatus.RECTIFIED.name().equals(invoice.status())) {
			throw new ApiValidationException("Payments can only be registered for issued invoices.");
		}
		if (body.amount().compareTo(invoice.outstandingAmount()) > 0) {
			throw new ApiValidationException("Payment amount cannot exceed the outstanding amount.");
		}
		InvoicePayment payment = payments.save(InvoicePayment.create(
				membership.tenant(),
				invoice.issuerCompany(),
				invoice,
				body.amount().setScale(2, RoundingMode.HALF_UP),
				body.paymentDate() == null ? LocalDate.now() : body.paymentDate(),
				body.method().trim().toUpperCase(),
				normalizeText(body.reference()),
				normalizeText(body.notes()),
				tenantAccess.currentUser().id()));
		invoice.registerPayment(payment.amount());
		auditEvents.record(tenantId, invoice.issuerCompany().id(), "INVOICE_PAYMENT_REGISTERED", "INVOICE", invoice.id(), payment.amount().toPlainString());
		return InvoicePaymentResponse.from(payment);
	}

	@Transactional
	public InvoicePdfDownload downloadPdf(String tenantId, String invoiceId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		FiscalInvoice invoice = requireInvoice(tenantId, invoiceId);
		if (InvoiceStatus.DRAFT.name().equals(invoice.status())) {
			throw new ApiValidationException("PDF is available after issuing the invoice.");
		}
		InvoicePdfDownload download = pdfService.render(invoice, lines.findByInvoice_IdOrderByLineNumberAsc(invoice.id()));
		artifacts.save(InvoiceArtifact.create(
				invoice.tenant(),
				invoice.issuerCompany(),
				invoice,
				"PDF",
				download.filename(),
				download.sha256(),
				tenantAccess.currentUser().id()));
		auditEvents.record(tenantId, invoice.issuerCompany().id(), "PDF_GENERATED", "INVOICE", invoice.id(), download.sha256());
		return download;
	}

	private Calculation calculate(List<CreateInvoiceLineRequest> requestLines) {
		if (requestLines == null || requestLines.isEmpty()) {
			throw new ApiValidationException("At least one invoice line is required.");
		}
		List<CalculatedLine> calculatedLines = new ArrayList<>();
		for (int index = 0; index < requestLines.size(); index++) {
			calculatedLines.add(calculateLine(requestLines.get(index), index + 1));
		}
		BigDecimal grossTotal = calculatedLines.stream().map(CalculatedLine::grossBase).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal taxableBase = calculatedLines.stream().map(CalculatedLine::lineBase).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal taxTotal = calculatedLines.stream().map(CalculatedLine::taxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal withholdingTotal = calculatedLines.stream().map(CalculatedLine::withholdingAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
		Map<BigDecimal, BigDecimal[]> groupedTaxes = new LinkedHashMap<>();
		for (CalculatedLine line : calculatedLines) {
			groupedTaxes.computeIfAbsent(line.taxRate(), ignored -> new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO});
			groupedTaxes.get(line.taxRate())[0] = groupedTaxes.get(line.taxRate())[0].add(line.lineBase());
			groupedTaxes.get(line.taxRate())[1] = groupedTaxes.get(line.taxRate())[1].add(line.taxAmount());
		}
		List<CalculatedTax> calculatedTaxes = groupedTaxes.entrySet()
				.stream()
				.sorted(Comparator.comparing(Map.Entry::getKey))
				.map(entry -> new CalculatedTax(entry.getKey(), entry.getValue()[0], entry.getValue()[1]))
				.toList();
		return new Calculation(
				calculatedLines,
				calculatedTaxes,
				taxableBase,
				taxTotal,
				withholdingTotal,
				grossTotal,
				taxableBase,
				taxableBase.add(taxTotal).subtract(withholdingTotal));
	}

	private CalculatedLine calculateLine(CreateInvoiceLineRequest line, int lineNumber) {
		if (line.description() == null || line.description().isBlank()) {
			throw new ApiValidationException("Line description is required.");
		}
		BigDecimal quantity = positive(line.quantity(), "quantity");
		BigDecimal unitPrice = nonNegative(line.unitPrice(), "unitPrice");
		BigDecimal taxRate = nonNegative(line.taxRate(), "taxRate").setScale(2, RoundingMode.HALF_UP);
		BigDecimal discountPercent = nonNegative(defaultZero(line.discountPercent()), "discountPercent").setScale(2, RoundingMode.HALF_UP);
		BigDecimal withholdingPercent = nonNegative(defaultZero(line.withholdingPercent()), "withholdingPercent").setScale(2, RoundingMode.HALF_UP);
		if (discountPercent.compareTo(ONE_HUNDRED) > 0) {
			throw new ApiValidationException("discountPercent cannot exceed 100.");
		}
		if (withholdingPercent.compareTo(ONE_HUNDRED) > 0) {
			throw new ApiValidationException("withholdingPercent cannot exceed 100.");
		}
		BigDecimal grossBase = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
		BigDecimal discountAmount = grossBase.multiply(discountPercent).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
		BigDecimal lineBase = grossBase.subtract(discountAmount);
		BigDecimal taxAmount = lineBase.multiply(taxRate).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
		BigDecimal withholdingAmount = lineBase.multiply(withholdingPercent).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
		return new CalculatedLine(
				lineNumber,
				line.description().trim(),
				quantity,
				unitPrice,
				taxRate,
				discountPercent,
				withholdingPercent,
				withholdingAmount,
				line.taxCategory() == null || line.taxCategory().isBlank() ? "STANDARD" : line.taxCategory().trim().toUpperCase(),
				grossBase,
				lineBase,
				taxAmount,
				lineBase.add(taxAmount).subtract(withholdingAmount));
	}

	private void storeCalculatedDetails(FiscalInvoice invoice, Calculation calculation) {
		lines.saveAll(calculation.lines().stream()
				.map(line -> FiscalInvoiceLine.create(
						invoice,
						line.lineNumber(),
						line.description(),
						line.quantity(),
						line.unitPrice(),
						line.taxRate(),
						line.discountPercent(),
						line.withholdingPercent(),
						line.withholdingAmount(),
						line.taxCategory(),
						line.lineBase(),
						line.taxAmount(),
						line.lineTotal()))
				.toList());
		taxes.saveAll(calculation.taxes().stream()
				.map(tax -> FiscalInvoiceTax.create(invoice, tax.taxRate(), tax.taxableBase(), tax.taxAmount()))
				.toList());
	}

	private void requireFiscalSettingsReady(CompanyFiscalSettings settings) {
		if (settings.nif() == null || settings.nif().isBlank()
				|| settings.addressLine1() == null || settings.addressLine1().isBlank()
				|| settings.city() == null || settings.city().isBlank()
				|| settings.postalCode() == null || settings.postalCode().isBlank()
				|| settings.defaultCurrency() == null || settings.defaultCurrency().isBlank()) {
			throw new ApiValidationException("Fiscal settings are incomplete for issuing invoices.");
		}
	}

	private InvoiceSeries resolveSeriesForIssue(String tenantId, String issuerCompanyId, String requestedSeriesId) {
		if (requestedSeriesId != null && !requestedSeriesId.isBlank()) {
			InvoiceSeries series = seriesRepository.findByIdAndTenant_Id(requestedSeriesId.trim(), tenantId)
					.orElseThrow(() -> new ResourceNotFoundException("Invoice series was not found."));
			if (!issuerCompanyId.equals(series.company().id())) {
				throw new ResourceNotFoundException("Invoice series was not found for the issuer company.");
			}
			if (!series.active()) {
				throw new ApiValidationException("Invoice series is not active.");
			}
			return series;
		}
		return seriesRepository.findFirstByCompany_IdAndTenant_IdAndActiveTrueOrderByCodeAsc(issuerCompanyId, tenantId)
				.orElseThrow(() -> new ApiValidationException("An active invoice series is required before issuing invoices."));
	}

	private LocalDate resolveDueDate(String tenantId, String companyId, LocalDate issueDate, LocalDate dueDate) {
		if (dueDate != null) {
			return dueDate;
		}
		return issueDate.plusDays(fiscalSettings.findByCompany_IdAndTenant_Id(companyId, tenantId)
				.map(CompanyFiscalSettings::defaultPaymentTermsDays)
				.orElse(30));
	}

	private Customer resolveCustomer(String tenantId, String customerId) {
		if (customerId == null || customerId.isBlank()) {
			return null;
		}
		return customers.findByIdAndTenant_Id(customerId.trim(), tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer was not found in the tenant."));
	}

	private Map<String, Object> issuerSnapshot(CompanyFiscalSettings settings) {
		Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("legalName", settings.legalName());
		snapshot.put("nif", settings.nif());
		snapshot.put("vatNumber", settings.vatNumber());
		snapshot.put("addressLine1", settings.addressLine1());
		snapshot.put("city", settings.city());
		snapshot.put("postalCode", settings.postalCode());
		snapshot.put("country", settings.country());
		snapshot.put("sifMode", settings.sifMode());
		snapshot.put("verifactuLabelEnabled", settings.verifactuLabelEnabled());
		return snapshot;
	}

	private Map<String, Object> customerSnapshot(FiscalInvoice invoice) {
		if (invoice.customer() != null) {
			return invoice.customer().snapshot();
		}
		Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("companyId", invoice.customerCompany().id());
		snapshot.put("name", invoice.customerCompany().legalName());
		snapshot.put("nif", invoice.customerCompany().taxId());
		snapshot.put("country", invoice.customerCompany().countryCode());
		return snapshot;
	}

	private Map<String, Object> totalsSnapshot(FiscalInvoice invoice) {
		Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("taxableBase", invoice.taxableBase());
		snapshot.put("taxTotal", invoice.taxTotal());
		snapshot.put("withholdingTotal", invoice.withholdingTotal());
		snapshot.put("grossTotal", invoice.grossTotal());
		snapshot.put("netTotal", invoice.netTotal());
		snapshot.put("payableTotal", invoice.payableTotal());
		snapshot.put("currency", invoice.currency());
		return snapshot;
	}

	private String writeJson(Map<String, Object> payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Unable to write invoice snapshot.", exception);
		}
	}

	private BigDecimal positive(BigDecimal value, String field) {
		if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ApiValidationException(field + " must be positive.");
		}
		return value;
	}

	private BigDecimal nonNegative(BigDecimal value, String field) {
		if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
			throw new ApiValidationException(field + " must be non-negative.");
		}
		return value;
	}

	private BigDecimal defaultZero(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private String normalizeStatus(String status) {
		if (status == null || status.isBlank()) {
			return null;
		}
		return InvoiceStatus.fromValue(status).name();
	}

	private String normalizeText(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	private String trimToMax(String value, int max) {
		return value.length() <= max ? value : value.substring(0, max);
	}

	private String nullToDash(Object value) {
		return value == null ? "-" : value.toString();
	}

	private Membership requireWriteAccess(String tenantId, HttpServletRequest request) {
		Membership membership = tenantAccess.requireTenantAccess(tenantId, request);
		if (!WRITE_ROLES.contains(membership.fiscalRole())) {
			throw new TenantAccessDeniedException("The current role cannot mutate fiscal invoices.");
		}
		return membership;
	}

	private Company requireCompany(String tenantId, String companyId) {
		return companies.findByIdAndTenantId(companyId, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Company was not found in the tenant."));
	}

	private FiscalInvoice requireInvoice(String tenantId, String invoiceId) {
		return invoices.findByIdAndTenant_Id(invoiceId, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Invoice was not found in the tenant."));
	}

	private InvoiceResponse toResponse(FiscalInvoice invoice) {
		return InvoiceResponse.from(
				invoice,
				lines.findByInvoice_IdOrderByLineNumberAsc(invoice.id()),
				taxes.findByInvoice_IdOrderByTaxRateAsc(invoice.id()));
	}

	private record Calculation(
			List<CalculatedLine> lines,
			List<CalculatedTax> taxes,
			BigDecimal taxableBase,
			BigDecimal taxTotal,
			BigDecimal withholdingTotal,
			BigDecimal grossTotal,
			BigDecimal netTotal,
			BigDecimal total) {
	}

	private record CalculatedLine(
			int lineNumber,
			String description,
			BigDecimal quantity,
			BigDecimal unitPrice,
			BigDecimal taxRate,
			BigDecimal discountPercent,
			BigDecimal withholdingPercent,
			BigDecimal withholdingAmount,
			String taxCategory,
			BigDecimal grossBase,
			BigDecimal lineBase,
			BigDecimal taxAmount,
			BigDecimal lineTotal) {
	}

	private record CalculatedTax(BigDecimal taxRate, BigDecimal taxableBase, BigDecimal taxAmount) {
	}
}

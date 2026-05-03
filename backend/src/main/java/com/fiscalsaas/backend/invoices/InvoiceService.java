package com.fiscalsaas.backend.invoices;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fiscalsaas.backend.api.ApiConflictException;
import com.fiscalsaas.backend.api.ApiValidationException;
import com.fiscalsaas.backend.api.ResourceNotFoundException;
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

	InvoiceService(
			TenantAccessService tenantAccess,
			CompanyRepository companies,
			FiscalInvoiceRepository invoices,
			FiscalInvoiceLineRepository lines,
			FiscalInvoiceTaxRepository taxes) {
		this.tenantAccess = tenantAccess;
		this.companies = companies;
		this.invoices = invoices;
		this.lines = lines;
		this.taxes = taxes;
	}

	@Transactional(readOnly = true)
	public List<InvoiceResponse> listInvoices(String tenantId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		return invoices.findByTenant_IdOrderByIssueDateDescInvoiceNumberDesc(tenantId)
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
		Company customer = requireCompany(tenantId, body.customerCompanyId());
		FiscalInvoice rectifies = body.rectifiesInvoiceId() == null || body.rectifiesInvoiceId().isBlank()
				? null
				: requireInvoice(tenantId, body.rectifiesInvoiceId());
		if (invoiceType == InvoiceType.RECTIFYING && rectifies == null) {
			throw new ApiValidationException("rectifiesInvoiceId is required for RECTIFYING invoices.");
		}

		Calculation calculation = calculate(body.lines());
		FiscalInvoice invoice = invoices.save(FiscalInvoice.create(
				membership.tenant(),
				issuer,
				customer,
				body.invoiceNumber(),
				invoiceType,
				body.issueDate() == null ? LocalDate.now() : body.issueDate(),
				body.currency() == null || body.currency().isBlank() ? "EUR" : body.currency(),
				calculation.taxableBase(),
				calculation.taxTotal(),
				calculation.total(),
				rectifies,
				tenantAccess.currentUser().id()));
		lines.saveAll(calculation.lines().stream()
				.map(line -> FiscalInvoiceLine.create(
						invoice,
						line.lineNumber(),
						line.description(),
						line.quantity(),
						line.unitPrice(),
						line.taxRate(),
						line.lineBase(),
						line.taxAmount(),
						line.lineTotal()))
				.toList());
		taxes.saveAll(calculation.taxes().stream()
				.map(tax -> FiscalInvoiceTax.create(invoice, tax.taxRate(), tax.taxableBase(), tax.taxAmount()))
				.toList());
		return toResponse(invoice);
	}

	@Transactional
	public InvoiceResponse updateStatus(String tenantId, String invoiceId, UpdateInvoiceStatusRequest body, HttpServletRequest request) {
		requireWriteAccess(tenantId, request);
		FiscalInvoice invoice = requireInvoice(tenantId, invoiceId);
		invoice.updateStatus(InvoiceStatus.fromValue(body.status()));
		return toResponse(invoice);
	}

	private Calculation calculate(List<CreateInvoiceLineRequest> requestLines) {
		if (requestLines == null || requestLines.isEmpty()) {
			throw new ApiValidationException("At least one invoice line is required.");
		}
		List<CalculatedLine> calculatedLines = new ArrayList<>();
		for (int index = 0; index < requestLines.size(); index++) {
			calculatedLines.add(calculateLine(requestLines.get(index), index + 1));
		}
		BigDecimal taxableBase = calculatedLines.stream().map(CalculatedLine::lineBase).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal taxTotal = calculatedLines.stream().map(CalculatedLine::taxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
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
		return new Calculation(calculatedLines, calculatedTaxes, taxableBase, taxTotal, taxableBase.add(taxTotal));
	}

	private CalculatedLine calculateLine(CreateInvoiceLineRequest line, int lineNumber) {
		if (line.description() == null || line.description().isBlank()) {
			throw new ApiValidationException("Line description is required.");
		}
		BigDecimal quantity = positive(line.quantity(), "quantity");
		BigDecimal unitPrice = nonNegative(line.unitPrice(), "unitPrice");
		BigDecimal taxRate = nonNegative(line.taxRate(), "taxRate").setScale(2, RoundingMode.HALF_UP);
		BigDecimal lineBase = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
		BigDecimal taxAmount = lineBase.multiply(taxRate).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
		return new CalculatedLine(
				lineNumber,
				line.description().trim(),
				quantity,
				unitPrice,
				taxRate,
				lineBase,
				taxAmount,
				lineBase.add(taxAmount));
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
			BigDecimal total) {
	}

	private record CalculatedLine(
			int lineNumber,
			String description,
			BigDecimal quantity,
			BigDecimal unitPrice,
			BigDecimal taxRate,
			BigDecimal lineBase,
			BigDecimal taxAmount,
			BigDecimal lineTotal) {
	}

	private record CalculatedTax(BigDecimal taxRate, BigDecimal taxableBase, BigDecimal taxAmount) {
	}
}

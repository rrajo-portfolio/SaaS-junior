package com.fiscalsaas.backend.fiscal;

import java.util.EnumSet;
import java.util.List;
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
import com.fiscalsaas.backend.invoices.FiscalInvoiceRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FiscalConfigurationService {

	private static final Set<FiscalRole> ADMIN_ROLES = EnumSet.of(
			FiscalRole.PLATFORM_ADMIN,
			FiscalRole.TENANT_ADMIN,
			FiscalRole.FISCAL_MANAGER);

	private final TenantAccessService tenantAccess;
	private final CompanyRepository companies;
	private final CompanyFiscalSettingsRepository settings;
	private final InvoiceSeriesRepository series;
	private final FiscalInvoiceRepository invoices;
	private final AuditEventService auditEvents;

	FiscalConfigurationService(
			TenantAccessService tenantAccess,
			CompanyRepository companies,
			CompanyFiscalSettingsRepository settings,
			InvoiceSeriesRepository series,
			FiscalInvoiceRepository invoices,
			AuditEventService auditEvents) {
		this.tenantAccess = tenantAccess;
		this.companies = companies;
		this.settings = settings;
		this.series = series;
		this.invoices = invoices;
		this.auditEvents = auditEvents;
	}

	@Transactional(readOnly = true)
	public FiscalSettingsResponse getSettings(String tenantId, String companyId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		requireCompany(tenantId, companyId);
		return settings.findByCompany_IdAndTenant_Id(companyId, tenantId)
				.map(FiscalSettingsResponse::from)
				.orElseThrow(() -> new ResourceNotFoundException("Fiscal settings were not found for the company."));
	}

	@Transactional
	public FiscalSettingsResponse upsertSettings(String tenantId, String companyId, FiscalSettingsRequest body, HttpServletRequest request) {
		Membership membership = requireAdminAccess(tenantId, request);
		Company company = requireCompany(tenantId, companyId);
		CompanyFiscalSettings current = settings.findByCompany_IdAndTenant_Id(companyId, tenantId)
				.orElseGet(() -> CompanyFiscalSettings.create(membership.tenant(), company, body));
		if (current.id() != null) {
			try {
				current.apply(body);
			} catch (IllegalArgumentException exception) {
				throw new ApiValidationException(exception.getMessage());
			}
		}
		CompanyFiscalSettings saved = settings.save(current);
		auditEvents.record(tenantId, companyId, "FISCAL_SETTINGS_UPDATED", "COMPANY", companyId, "Fiscal settings updated");
		return FiscalSettingsResponse.from(saved);
	}

	@Transactional(readOnly = true)
	public List<InvoiceSeriesResponse> listSeries(String tenantId, String companyId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		requireCompany(tenantId, companyId);
		return series.findByCompany_IdAndTenant_IdOrderByCodeAsc(companyId, tenantId)
				.stream()
				.map(InvoiceSeriesResponse::from)
				.toList();
	}

	@Transactional
	public InvoiceSeriesResponse createSeries(String tenantId, String companyId, InvoiceSeriesRequest body, HttpServletRequest request) {
		Membership membership = requireAdminAccess(tenantId, request);
		Company company = requireCompany(tenantId, companyId);
		String code = body.code().trim().toUpperCase();
		if (series.existsByCompany_IdAndTenant_IdAndCode(companyId, tenantId, code)) {
			throw new ApiConflictException("Invoice series code already exists for this company.");
		}
		InvoiceSeries saved = series.save(InvoiceSeries.create(membership.tenant(), company, body));
		auditEvents.record(tenantId, companyId, "INVOICE_SERIES_CREATED", "INVOICE_SERIES", saved.id(), "Series " + saved.code() + " created");
		return InvoiceSeriesResponse.from(saved);
	}

	@Transactional
	public InvoiceSeriesResponse updateSeries(String tenantId, String companyId, String seriesId, InvoiceSeriesRequest body, HttpServletRequest request) {
		requireAdminAccess(tenantId, request);
		requireCompany(tenantId, companyId);
		InvoiceSeries current = series.findByIdAndCompany_IdAndTenant_Id(seriesId, companyId, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Invoice series was not found for the company."));
		if (invoices.existsByTenant_IdAndSeriesCodeAndFiscalNumberIsNotNull(tenantId, current.code())
				&& (!current.code().equalsIgnoreCase(body.code()) || !current.prefix().equals(body.prefix()))) {
			throw new ApiValidationException("Series code and prefix cannot be changed after issued invoices exist.");
		}
		current.apply(body);
		auditEvents.record(tenantId, companyId, "INVOICE_SERIES_UPDATED", "INVOICE_SERIES", current.id(), "Series " + current.code() + " updated");
		return InvoiceSeriesResponse.from(current);
	}

	private Membership requireAdminAccess(String tenantId, HttpServletRequest request) {
		Membership membership = tenantAccess.requireTenantAccess(tenantId, request);
		if (!ADMIN_ROLES.contains(membership.fiscalRole())) {
			throw new TenantAccessDeniedException("The current role cannot manage fiscal configuration.");
		}
		return membership;
	}

	private Company requireCompany(String tenantId, String companyId) {
		return companies.findByIdAndTenantId(companyId, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Company was not found in the tenant."));
	}
}

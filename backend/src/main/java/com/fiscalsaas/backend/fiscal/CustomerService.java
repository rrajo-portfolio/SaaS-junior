package com.fiscalsaas.backend.fiscal;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.fiscalsaas.backend.api.ApiConflictException;
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
public class CustomerService {

	private static final Set<FiscalRole> WRITE_ROLES = EnumSet.of(
			FiscalRole.PLATFORM_ADMIN,
			FiscalRole.TENANT_ADMIN,
			FiscalRole.FISCAL_MANAGER,
			FiscalRole.ACCOUNTANT);

	private final TenantAccessService tenantAccess;
	private final CompanyRepository companies;
	private final CustomerRepository customers;
	private final AuditEventService auditEvents;

	CustomerService(
			TenantAccessService tenantAccess,
			CompanyRepository companies,
			CustomerRepository customers,
			AuditEventService auditEvents) {
		this.tenantAccess = tenantAccess;
		this.companies = companies;
		this.customers = customers;
		this.auditEvents = auditEvents;
	}

	@Transactional(readOnly = true)
	public List<CustomerResponse> listCustomers(String tenantId, String companyId, String search, String status, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		requireCompany(tenantId, companyId);
		String normalizedStatus = status == null || status.isBlank() ? null : status.trim().toUpperCase();
		String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
		return customers.search(tenantId, companyId, normalizedSearch, normalizedStatus)
				.stream()
				.map(CustomerResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public CustomerResponse getCustomer(String tenantId, String companyId, String customerId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		return CustomerResponse.from(requireCustomer(tenantId, companyId, customerId));
	}

	@Transactional
	public CustomerResponse createCustomer(String tenantId, String companyId, CustomerRequest body, HttpServletRequest request) {
		Membership membership = requireWriteAccess(tenantId, request);
		Company company = requireCompany(tenantId, companyId);
		String nif = body.nif().trim().toUpperCase();
		if (customers.existsByCompany_IdAndTenant_IdAndNif(companyId, tenantId, nif)) {
			throw new ApiConflictException("Customer NIF already exists for this company.");
		}
		Customer saved = customers.save(Customer.create(membership.tenant(), company, body));
		auditEvents.record(tenantId, companyId, "CUSTOMER_CREATED", "CUSTOMER", saved.id(), saved.name());
		return CustomerResponse.from(saved);
	}

	@Transactional
	public CustomerResponse updateCustomer(String tenantId, String companyId, String customerId, CustomerRequest body, HttpServletRequest request) {
		requireWriteAccess(tenantId, request);
		Customer customer = requireCustomer(tenantId, companyId, customerId);
		String nif = body.nif().trim().toUpperCase();
		if (customers.existsByCompany_IdAndTenant_IdAndNifAndIdNot(companyId, tenantId, nif, customerId)) {
			throw new ApiConflictException("Customer NIF already exists for this company.");
		}
		customer.apply(body);
		auditEvents.record(tenantId, companyId, "CUSTOMER_UPDATED", "CUSTOMER", customer.id(), customer.name());
		return CustomerResponse.from(customer);
	}

	@Transactional
	public CustomerResponse deactivateCustomer(String tenantId, String companyId, String customerId, HttpServletRequest request) {
		requireWriteAccess(tenantId, request);
		Customer customer = requireCustomer(tenantId, companyId, customerId);
		customer.deactivate();
		auditEvents.record(tenantId, companyId, "CUSTOMER_DEACTIVATED", "CUSTOMER", customer.id(), customer.name());
		return CustomerResponse.from(customer);
	}

	private Membership requireWriteAccess(String tenantId, HttpServletRequest request) {
		Membership membership = tenantAccess.requireTenantAccess(tenantId, request);
		if (!WRITE_ROLES.contains(membership.fiscalRole())) {
			throw new TenantAccessDeniedException("The current role cannot mutate customers.");
		}
		return membership;
	}

	private Company requireCompany(String tenantId, String companyId) {
		return companies.findByIdAndTenantId(companyId, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Company was not found in the tenant."));
	}

	private Customer requireCustomer(String tenantId, String companyId, String customerId) {
		return customers.findByIdAndTenant_IdAndCompany_Id(customerId, tenantId, companyId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer was not found for the company."));
	}
}

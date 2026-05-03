package com.fiscalsaas.backend.companies;

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

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

	private static final Set<FiscalRole> WRITE_ROLES = EnumSet.of(
			FiscalRole.PLATFORM_ADMIN,
			FiscalRole.TENANT_ADMIN,
			FiscalRole.FISCAL_MANAGER,
			FiscalRole.ACCOUNTANT);

	private final TenantAccessService tenantAccess;
	private final CompanyRepository companies;
	private final BusinessRelationshipRepository relationships;
	private final TaxIdentifierValidator taxIdentifierValidator;

	CompanyService(
			TenantAccessService tenantAccess,
			CompanyRepository companies,
			BusinessRelationshipRepository relationships,
			TaxIdentifierValidator taxIdentifierValidator) {
		this.tenantAccess = tenantAccess;
		this.companies = companies;
		this.relationships = relationships;
		this.taxIdentifierValidator = taxIdentifierValidator;
	}

	@Transactional(readOnly = true)
	public List<CompanyResponse> listCompanies(String tenantId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		return companies.findByTenantIdOrderByLegalNameAsc(tenantId)
				.stream()
				.map(CompanyResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public CompanyResponse getCompany(String tenantId, String companyId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		return CompanyResponse.from(requireCompany(tenantId, companyId));
	}

	@Transactional
	public CompanyResponse createCompany(String tenantId, CreateCompanyRequest body, HttpServletRequest request) {
		Membership membership = requireWriteAccess(tenantId, request);
		CompanyRelationshipType relationshipType = CompanyRelationshipType.fromValue(body.relationshipType());
		TaxIdentifierValidator.ValidatedTaxId taxId = taxIdentifierValidator.validate(body.countryCode(), body.taxId());
		String legalName = requireText(body.legalName(), "legalName");
		if (companies.existsByTenantIdAndTaxId(tenantId, taxId.taxId())) {
			throw new ApiConflictException("A company with this taxId already exists in the tenant.");
		}
		Company company = Company.create(membership.tenant(), legalName, taxId.taxId(), taxId.countryCode(), relationshipType.name());
		return CompanyResponse.from(companies.save(company));
	}

	@Transactional
	public CompanyResponse updateCompany(
			String tenantId,
			String companyId,
			UpdateCompanyRequest body,
			HttpServletRequest request) {
		requireWriteAccess(tenantId, request);
		Company company = requireCompany(tenantId, companyId);
		String legalName = textOrCurrent(body.legalName(), company.legalName(), "legalName");
		String countryCode = textOrCurrent(body.countryCode(), company.countryCode(), "countryCode");
		String rawTaxId = textOrCurrent(body.taxId(), company.taxId(), "taxId");
		TaxIdentifierValidator.ValidatedTaxId taxId = taxIdentifierValidator.validate(countryCode, rawTaxId);
		CompanyRelationshipType relationshipType = CompanyRelationshipType.fromValue(
				textOrCurrent(body.relationshipType(), company.relationshipType(), "relationshipType"));
		String status = parseStatus(textOrCurrent(body.status(), company.status(), "status"));
		if (companies.existsByTenantIdAndTaxIdAndIdNot(tenantId, taxId.taxId(), companyId)) {
			throw new ApiConflictException("A company with this taxId already exists in the tenant.");
		}
		company.update(legalName, taxId.taxId(), taxId.countryCode(), relationshipType.name(), status);
		return CompanyResponse.from(company);
	}

	@Transactional
	public void deactivateCompany(String tenantId, String companyId, HttpServletRequest request) {
		requireWriteAccess(tenantId, request);
		requireCompany(tenantId, companyId).deactivate();
	}

	@Transactional(readOnly = true)
	public List<BusinessRelationshipResponse> listRelationships(String tenantId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		return relationships.findByTenant_IdOrderByCreatedAtDesc(tenantId)
				.stream()
				.map(BusinessRelationshipResponse::from)
				.toList();
	}

	@Transactional
	public BusinessRelationshipResponse createRelationship(
			String tenantId,
			CreateBusinessRelationshipRequest body,
			HttpServletRequest request) {
		Membership membership = requireWriteAccess(tenantId, request);
		BusinessRelationshipKind kind = BusinessRelationshipKind.fromValue(body.relationshipKind());
		Company sourceCompany = requireCompany(tenantId, body.sourceCompanyId());
		Company targetCompany = requireCompany(tenantId, body.targetCompanyId());
		if (sourceCompany.id().equals(targetCompany.id())) {
			throw new ApiValidationException("sourceCompanyId and targetCompanyId must be different.");
		}
		if (relationships.existsByTenant_IdAndSourceCompany_IdAndTargetCompany_IdAndRelationshipKind(
				tenantId, sourceCompany.id(), targetCompany.id(), kind.name())) {
			throw new ApiConflictException("This business relationship already exists in the tenant.");
		}
		BusinessRelationship relationship = BusinessRelationship.create(
				membership.tenant(), sourceCompany, targetCompany, kind, body.notes());
		return BusinessRelationshipResponse.from(relationships.save(relationship));
	}

	@Transactional
	public BusinessRelationshipResponse updateRelationship(
			String tenantId,
			String relationshipId,
			UpdateBusinessRelationshipRequest body,
			HttpServletRequest request) {
		requireWriteAccess(tenantId, request);
		BusinessRelationship relationship = requireRelationship(tenantId, relationshipId);
		BusinessRelationshipKind kind = BusinessRelationshipKind.fromValue(
				textOrCurrent(body.relationshipKind(), relationship.relationshipKind(), "relationshipKind"));
		String status = parseStatus(textOrCurrent(body.status(), relationship.status(), "status"));
		relationship.update(kind, status, body.notes() == null ? relationship.notes() : body.notes());
		return BusinessRelationshipResponse.from(relationship);
	}

	@Transactional
	public void deactivateRelationship(String tenantId, String relationshipId, HttpServletRequest request) {
		requireWriteAccess(tenantId, request);
		requireRelationship(tenantId, relationshipId).deactivate();
	}

	private Membership requireWriteAccess(String tenantId, HttpServletRequest request) {
		Membership membership = tenantAccess.requireTenantAccess(tenantId, request);
		if (!WRITE_ROLES.contains(membership.fiscalRole())) {
			throw new TenantAccessDeniedException("The current role cannot mutate tenant company data.");
		}
		return membership;
	}

	private Company requireCompany(String tenantId, String companyId) {
		return companies.findByIdAndTenantId(companyId, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Company was not found in the tenant."));
	}

	private BusinessRelationship requireRelationship(String tenantId, String relationshipId) {
		return relationships.findByIdAndTenant_Id(relationshipId, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Business relationship was not found in the tenant."));
	}

	private String requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new ApiValidationException(field + " is required.");
		}
		return value.trim();
	}

	private String textOrCurrent(String value, String current, String field) {
		if (value == null) {
			return current;
		}
		return requireText(value, field);
	}

	private String parseStatus(String value) {
		String normalized = value.trim().toUpperCase();
		if (!"ACTIVE".equals(normalized) && !"INACTIVE".equals(normalized)) {
			throw new ApiValidationException("Unsupported status.");
		}
		return normalized;
	}
}

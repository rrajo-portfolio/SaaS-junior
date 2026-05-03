package com.fiscalsaas.backend.api;

import java.util.List;

import com.fiscalsaas.backend.identity.Company;
import com.fiscalsaas.backend.identity.CompanyRepository;
import com.fiscalsaas.backend.identity.FiscalRole;
import com.fiscalsaas.backend.identity.Membership;
import com.fiscalsaas.backend.identity.TenantAccessService;
import com.fiscalsaas.backend.security.CurrentUser;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class IdentityController {

	private final TenantAccessService tenantAccess;
	private final CompanyRepository companies;

	IdentityController(TenantAccessService tenantAccess, CompanyRepository companies) {
		this.tenantAccess = tenantAccess;
		this.companies = companies;
	}

	@GetMapping("/me")
	MeResponse me() {
		CurrentUser user = tenantAccess.currentUser();
		List<MembershipResponse> memberships = tenantAccess.currentMemberships()
				.stream()
				.map(MembershipResponse::from)
				.toList();
		return new MeResponse(new UserResponse(user.id(), user.email(), user.displayName(), user.roles()), memberships);
	}

	@GetMapping("/tenants")
	List<TenantResponse> tenants() {
		return tenantAccess.currentMemberships()
				.stream()
				.map(membership -> new TenantResponse(
						membership.tenant().id(),
						membership.tenant().slug(),
						membership.tenant().displayName(),
						membership.fiscalRole()))
				.toList();
	}

	@GetMapping("/tenants/{tenantId}/companies")
	List<CompanyResponse> companies(@PathVariable String tenantId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		return companies.findByTenantIdOrderByLegalNameAsc(tenantId)
				.stream()
				.map(CompanyResponse::from)
				.toList();
	}

	record MeResponse(UserResponse user, List<MembershipResponse> memberships) {
	}

	record UserResponse(String id, String email, String displayName, List<FiscalRole> roles) {
	}

	record MembershipResponse(String tenantId, String tenantSlug, String tenantName, FiscalRole role) {
		static MembershipResponse from(Membership membership) {
			return new MembershipResponse(
					membership.tenant().id(),
					membership.tenant().slug(),
					membership.tenant().displayName(),
					membership.fiscalRole());
		}
	}

	record TenantResponse(String id, String slug, String name, FiscalRole role) {
	}

	record CompanyResponse(
			String id,
			String tenantId,
			String legalName,
			String taxId,
			String countryCode,
			String relationshipType,
			String status) {
		static CompanyResponse from(Company company) {
			return new CompanyResponse(
					company.id(),
					company.tenantId(),
					company.legalName(),
					company.taxId(),
					company.countryCode(),
					company.relationshipType(),
					company.status());
		}
	}
}

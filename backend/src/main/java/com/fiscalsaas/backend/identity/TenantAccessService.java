package com.fiscalsaas.backend.identity;

import java.util.List;

import com.fiscalsaas.backend.security.CurrentUser;
import com.fiscalsaas.backend.security.HeaderAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TenantAccessService {

	private final MembershipRepository memberships;

	TenantAccessService(MembershipRepository memberships) {
		this.memberships = memberships;
	}

	public CurrentUser currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
			throw new TenantAccessDeniedException("Authenticated user is required.");
		}
		return currentUser;
	}

	public List<Membership> currentMemberships() {
		return memberships.findByUserIdAndStatusOrderByTenantDisplayNameAsc(currentUser().id(), "ACTIVE");
	}

	public Membership requireTenantAccess(String tenantId, HttpServletRequest request) {
		String headerTenant = request.getHeader(HeaderAuthenticationFilter.TENANT_ID_HEADER);
		if (headerTenant == null || headerTenant.isBlank()) {
			throw new TenantAccessDeniedException("Tenant header is required.");
		}
		if (!tenantId.equals(headerTenant.trim())) {
			throw new TenantAccessDeniedException("Tenant header does not match requested tenant.");
		}
		CurrentUser user = currentUser();
		return memberships.findByTenantIdAndUserIdAndStatus(tenantId, user.id(), "ACTIVE")
				.orElseThrow(() -> new TenantAccessDeniedException("User is not a member of the requested tenant."));
	}
}

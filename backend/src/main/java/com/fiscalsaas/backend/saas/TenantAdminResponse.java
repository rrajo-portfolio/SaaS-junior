package com.fiscalsaas.backend.saas;

import java.time.Instant;

import com.fiscalsaas.backend.identity.Tenant;

public record TenantAdminResponse(
		String id,
		String slug,
		String displayName,
		String status,
		String planCode,
		String subscriptionStatus,
		Instant trialEndsAt,
		Instant suspendedAt) {

	static TenantAdminResponse from(Tenant tenant) {
		return new TenantAdminResponse(
				tenant.id(),
				tenant.slug(),
				tenant.displayName(),
				tenant.status(),
				tenant.planCode(),
				tenant.subscriptionStatus(),
				tenant.trialEndsAt(),
				tenant.suspendedAt());
	}
}

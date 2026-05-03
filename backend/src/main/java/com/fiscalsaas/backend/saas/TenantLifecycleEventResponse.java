package com.fiscalsaas.backend.saas;

import java.time.Instant;

public record TenantLifecycleEventResponse(
		String id,
		String tenantId,
		String eventType,
		String actorEmail,
		String fromStatus,
		String toStatus,
		String fromPlanCode,
		String toPlanCode,
		String notes,
		Instant createdAt) {

	static TenantLifecycleEventResponse from(TenantLifecycleEvent event) {
		return new TenantLifecycleEventResponse(
				event.id(),
				event.tenantId(),
				event.eventType(),
				event.actorEmail(),
				event.fromStatus(),
				event.toStatus(),
				event.fromPlanCode(),
				event.toPlanCode(),
				event.notes(),
				event.createdAt());
	}
}

package com.fiscalsaas.backend.fiscal;

import java.time.Instant;

public record AuditEventResponse(
		String id,
		String tenantId,
		String companyId,
		String actorEmail,
		String eventType,
		String entityType,
		String entityId,
		String details,
		String previousHash,
		String eventHash,
		Instant occurredAt) {
	static AuditEventResponse from(AuditEvent event) {
		return new AuditEventResponse(
				event.id(),
				event.tenantId(),
				event.companyId(),
				event.actorEmail(),
				event.eventType(),
				event.entityType(),
				event.entityId(),
				event.details(),
				event.previousHash(),
				event.eventHash(),
				event.occurredAt());
	}
}

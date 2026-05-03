package com.fiscalsaas.backend.documents;

import java.time.Instant;

public record DocumentEventResponse(String eventType, Instant eventAt, String details) {
	static DocumentEventResponse from(DocumentAuditEvent event) {
		return new DocumentEventResponse(event.eventType(), event.eventAt(), event.details());
	}
}

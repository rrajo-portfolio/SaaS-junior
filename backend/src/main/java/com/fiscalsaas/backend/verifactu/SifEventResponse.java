package com.fiscalsaas.backend.verifactu;

import java.time.Instant;

public record SifEventResponse(String id, String recordId, String eventType, Instant eventAt, String details) {
	static SifEventResponse from(SifEventLog event) {
		return new SifEventResponse(event.id(), event.recordId(), event.eventType(), event.eventAt(), event.details());
	}
}

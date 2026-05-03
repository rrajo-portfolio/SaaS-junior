package com.fiscalsaas.backend.einvoice;

import java.time.Instant;

public record EInvoiceEventResponse(String id, String messageId, String eventType, String details, Instant eventAt) {
	static EInvoiceEventResponse from(EInvoiceEvent event) {
		return new EInvoiceEventResponse(
				event.id(),
				event.messageId(),
				event.eventType(),
				event.details(),
				event.eventAt());
	}
}

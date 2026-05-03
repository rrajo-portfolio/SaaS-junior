package com.fiscalsaas.backend.einvoice;

import java.math.BigDecimal;
import java.time.Instant;

public record EInvoicePaymentEventResponse(
		String id,
		String messageId,
		String paymentStatus,
		BigDecimal amount,
		Instant paidAt,
		String paymentReference,
		String notes,
		Instant createdAt) {
	static EInvoicePaymentEventResponse from(EInvoicePaymentEvent event) {
		return new EInvoicePaymentEventResponse(
				event.id(),
				event.messageId(),
				event.paymentStatus(),
				event.amount(),
				event.paidAt(),
				event.paymentReference(),
				event.notes(),
				event.createdAt());
	}
}

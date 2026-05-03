package com.fiscalsaas.backend.einvoice;

import java.time.Instant;

public record EInvoiceMessageResponse(
		String id,
		String tenantId,
		String invoiceId,
		String invoiceNumber,
		String issuerLegalName,
		String customerLegalName,
		String syntax,
		String direction,
		String exchangeStatus,
		String commercialStatus,
		String paymentStatus,
		String payloadSha256,
		String statusReason,
		Instant createdAt,
		Instant updatedAt) {
	static EInvoiceMessageResponse from(EInvoiceMessage message) {
		return new EInvoiceMessageResponse(
				message.id(),
				message.tenantId(),
				message.invoice().id(),
				message.invoice().invoiceNumber(),
				message.invoice().issuerCompany().legalName(),
				message.invoice().customerCompany().legalName(),
				message.syntax(),
				message.direction(),
				message.exchangeStatus(),
				message.commercialStatus(),
				message.paymentStatus(),
				message.payloadSha256(),
				message.statusReason(),
				message.createdAt(),
				message.updatedAt());
	}
}

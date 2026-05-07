package com.fiscalsaas.backend.invoices;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record InvoicePaymentResponse(
		String id,
		String tenantId,
		String companyId,
		String invoiceId,
		BigDecimal amount,
		LocalDate paymentDate,
		String method,
		String reference,
		String notes,
		Instant createdAt) {
	static InvoicePaymentResponse from(InvoicePayment payment) {
		return new InvoicePaymentResponse(
				payment.id(),
				payment.tenantId(),
				payment.companyId(),
				payment.invoiceId(),
				payment.amount(),
				payment.paymentDate(),
				payment.method(),
				payment.reference(),
				payment.notes(),
				payment.createdAt());
	}
}

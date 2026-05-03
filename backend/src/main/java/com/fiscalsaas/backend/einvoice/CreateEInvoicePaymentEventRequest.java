package com.fiscalsaas.backend.einvoice;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEInvoicePaymentEventRequest(
		@NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal amount,
		@NotNull @Size(max = 30) String paymentStatus,
		Instant paidAt,
		@Size(max = 120) String reference,
		@Size(max = 500) String notes) {
}

package com.fiscalsaas.backend.invoices;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePaymentRequest(
		@NotNull @DecimalMin(value = "0.01") BigDecimal amount,
		LocalDate paymentDate,
		@NotBlank @Size(max = 40) String method,
		@Size(max = 120) String reference,
		@Size(max = 500) String notes) {
}

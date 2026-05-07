package com.fiscalsaas.backend.invoices;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateInvoiceLineRequest(
		@NotBlank @Size(max = 500) String description,
		@NotNull @DecimalMin(value = "0.0001") BigDecimal quantity,
		@NotNull @DecimalMin(value = "0.00") BigDecimal unitPrice,
		@NotNull @DecimalMin(value = "0.00") BigDecimal taxRate,
		@DecimalMin(value = "0.00") BigDecimal discountPercent,
		@DecimalMin(value = "0.00") BigDecimal withholdingPercent,
		@Size(max = 40) String taxCategory) {
}

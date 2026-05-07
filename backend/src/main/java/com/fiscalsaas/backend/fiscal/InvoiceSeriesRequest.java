package com.fiscalsaas.backend.fiscal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InvoiceSeriesRequest(
		@NotBlank @Size(max = 40) String code,
		@NotBlank @Size(max = 80) String prefix,
		@Min(1) Long nextNumber,
		@Min(1) Integer padding,
		Boolean active) {
}

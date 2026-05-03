package com.fiscalsaas.backend.einvoice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEInvoiceRequest(
		@NotBlank @Size(max = 36) String invoiceId,
		@Size(max = 30) String syntax) {
}

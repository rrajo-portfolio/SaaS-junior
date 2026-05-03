package com.fiscalsaas.backend.einvoice;

import jakarta.validation.constraints.Size;

public record UpdateEInvoiceStatusRequest(
		@Size(max = 30) String exchangeStatus,
		@Size(max = 30) String commercialStatus,
		@Size(max = 500) String reason) {
}

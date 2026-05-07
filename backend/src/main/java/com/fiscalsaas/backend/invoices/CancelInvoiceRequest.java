package com.fiscalsaas.backend.invoices;

import jakarta.validation.constraints.Size;

public record CancelInvoiceRequest(@Size(max = 500) String reason) {
}

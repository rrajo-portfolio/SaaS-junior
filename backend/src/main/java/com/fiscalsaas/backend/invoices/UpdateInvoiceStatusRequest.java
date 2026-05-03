package com.fiscalsaas.backend.invoices;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateInvoiceStatusRequest(@NotBlank @Size(max = 30) String status) {
}

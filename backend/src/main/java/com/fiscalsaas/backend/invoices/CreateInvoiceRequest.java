package com.fiscalsaas.backend.invoices;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CreateInvoiceRequest(
		@NotBlank @Size(max = 36) String issuerCompanyId,
		@NotBlank @Size(max = 36) String customerCompanyId,
		@Size(max = 36) String customerId,
		@NotBlank @Size(max = 80) String invoiceNumber,
		@NotBlank @Size(max = 40) String invoiceType,
		LocalDate issueDate,
		LocalDate dueDate,
		@Size(min = 3, max = 3) String currency,
		@Size(max = 36) String rectifiesInvoiceId,
		@NotEmpty List<@Valid CreateInvoiceLineRequest> lines) {
}

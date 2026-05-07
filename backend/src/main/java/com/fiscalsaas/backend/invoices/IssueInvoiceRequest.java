package com.fiscalsaas.backend.invoices;

import jakarta.validation.constraints.Size;

public record IssueInvoiceRequest(
		@Size(max = 36) String seriesId,
		@Size(max = 80) String issueRequestId) {
}

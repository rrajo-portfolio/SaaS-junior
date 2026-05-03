package com.fiscalsaas.backend.invoices;

import java.math.BigDecimal;

public record InvoiceLineResponse(
		int lineNumber,
		String description,
		BigDecimal quantity,
		BigDecimal unitPrice,
		BigDecimal taxRate,
		BigDecimal lineBase,
		BigDecimal taxAmount,
		BigDecimal lineTotal) {
	static InvoiceLineResponse from(FiscalInvoiceLine line) {
		return new InvoiceLineResponse(
				line.lineNumber(),
				line.description(),
				line.quantity(),
				line.unitPrice(),
				line.taxRate(),
				line.lineBase(),
				line.taxAmount(),
				line.lineTotal());
	}
}

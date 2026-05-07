package com.fiscalsaas.backend.invoices;

import java.math.BigDecimal;

public record InvoiceLineResponse(
		int lineNumber,
		String description,
		BigDecimal quantity,
		BigDecimal unitPrice,
		BigDecimal taxRate,
		BigDecimal discountPercent,
		BigDecimal withholdingPercent,
		BigDecimal withholdingAmount,
		String taxCategory,
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
				line.discountPercent(),
				line.withholdingPercent(),
				line.withholdingAmount(),
				line.taxCategory(),
				line.lineBase(),
				line.taxAmount(),
				line.lineTotal());
	}
}

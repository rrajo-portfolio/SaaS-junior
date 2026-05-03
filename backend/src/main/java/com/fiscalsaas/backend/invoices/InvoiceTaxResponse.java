package com.fiscalsaas.backend.invoices;

import java.math.BigDecimal;

public record InvoiceTaxResponse(BigDecimal taxRate, BigDecimal taxableBase, BigDecimal taxAmount) {
	static InvoiceTaxResponse from(FiscalInvoiceTax tax) {
		return new InvoiceTaxResponse(tax.taxRate(), tax.taxableBase(), tax.taxAmount());
	}
}

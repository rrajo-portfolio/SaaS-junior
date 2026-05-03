package com.fiscalsaas.backend.invoices;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fiscalsaas.backend.companies.CompanyResponse;

public record InvoiceResponse(
		String id,
		String tenantId,
		CompanyResponse issuerCompany,
		CompanyResponse customerCompany,
		String invoiceNumber,
		String invoiceType,
		String status,
		LocalDate issueDate,
		String currency,
		BigDecimal taxableBase,
		BigDecimal taxTotal,
		BigDecimal total,
		String rectifiesInvoiceId,
		List<InvoiceLineResponse> lines,
		List<InvoiceTaxResponse> taxes) {
	static InvoiceResponse from(FiscalInvoice invoice, List<FiscalInvoiceLine> lines, List<FiscalInvoiceTax> taxes) {
		return new InvoiceResponse(
				invoice.id(),
				invoice.tenantId(),
				CompanyResponse.from(invoice.issuerCompany()),
				CompanyResponse.from(invoice.customerCompany()),
				invoice.invoiceNumber(),
				invoice.invoiceType(),
				invoice.status(),
				invoice.issueDate(),
				invoice.currency(),
				invoice.taxableBase(),
				invoice.taxTotal(),
				invoice.total(),
				invoice.rectifiesInvoiceId(),
				lines.stream().map(InvoiceLineResponse::from).toList(),
				taxes.stream().map(InvoiceTaxResponse::from).toList());
	}
}

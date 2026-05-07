package com.fiscalsaas.backend.invoices;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.fiscalsaas.backend.companies.CompanyResponse;

public record InvoiceResponse(
		String id,
		String tenantId,
		CompanyResponse issuerCompany,
		CompanyResponse customerCompany,
		String customerId,
		String invoiceNumber,
		String fiscalNumber,
		String seriesCode,
		String invoiceType,
		String status,
		LocalDate issueDate,
		LocalDate dueDate,
		Instant issuedAt,
		String currency,
		BigDecimal taxableBase,
		BigDecimal taxTotal,
		BigDecimal withholdingTotal,
		BigDecimal grossTotal,
		BigDecimal netTotal,
		BigDecimal payableTotal,
		BigDecimal total,
		String paymentStatus,
		BigDecimal paidAmount,
		BigDecimal outstandingAmount,
		String customerSnapshot,
		String issuerFiscalSnapshot,
		String totalsSnapshot,
		String cancellationReason,
		String rectifiesInvoiceId,
		List<InvoiceLineResponse> lines,
		List<InvoiceTaxResponse> taxes) {
	static InvoiceResponse from(FiscalInvoice invoice, List<FiscalInvoiceLine> lines, List<FiscalInvoiceTax> taxes) {
		return new InvoiceResponse(
				invoice.id(),
				invoice.tenantId(),
				CompanyResponse.from(invoice.issuerCompany()),
				CompanyResponse.from(invoice.customerCompany()),
				invoice.customerId(),
				invoice.invoiceNumber(),
				invoice.fiscalNumber(),
				invoice.seriesCode(),
				invoice.invoiceType(),
				invoice.status(),
				invoice.issueDate(),
				invoice.dueDate(),
				invoice.issuedAt(),
				invoice.currency(),
				invoice.taxableBase(),
				invoice.taxTotal(),
				invoice.withholdingTotal(),
				invoice.grossTotal(),
				invoice.netTotal(),
				invoice.payableTotal(),
				invoice.total(),
				invoice.paymentStatus(),
				invoice.paidAmount(),
				invoice.outstandingAmount(),
				invoice.customerSnapshot(),
				invoice.issuerFiscalSnapshot(),
				invoice.totalsSnapshot(),
				invoice.cancellationReason(),
				invoice.rectifiesInvoiceId(),
				lines.stream().map(InvoiceLineResponse::from).toList(),
				taxes.stream().map(InvoiceTaxResponse::from).toList());
	}
}

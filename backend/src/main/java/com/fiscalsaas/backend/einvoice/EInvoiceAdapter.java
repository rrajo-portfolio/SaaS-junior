package com.fiscalsaas.backend.einvoice;

import java.util.List;

import com.fiscalsaas.backend.invoices.FiscalInvoice;
import com.fiscalsaas.backend.invoices.FiscalInvoiceLine;
import com.fiscalsaas.backend.invoices.FiscalInvoiceTax;

public interface EInvoiceAdapter {
	EInvoiceSyntax syntax();

	String render(FiscalInvoice invoice, List<FiscalInvoiceLine> lines, List<FiscalInvoiceTax> taxes);
}

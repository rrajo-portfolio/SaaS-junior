package com.fiscalsaas.backend.einvoice;

import java.math.BigDecimal;
import java.util.List;

import com.fiscalsaas.backend.invoices.FiscalInvoice;
import com.fiscalsaas.backend.invoices.FiscalInvoiceLine;
import com.fiscalsaas.backend.invoices.FiscalInvoiceTax;

import org.springframework.stereotype.Component;

@Component
class UblEInvoiceAdapter implements EInvoiceAdapter {

	@Override
	public EInvoiceSyntax syntax() {
		return EInvoiceSyntax.UBL;
	}

	@Override
	public String render(FiscalInvoice invoice, List<FiscalInvoiceLine> lines, List<FiscalInvoiceTax> taxes) {
		StringBuilder xml = new StringBuilder();
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		xml.append("<ubl:Invoice xmlns:ubl=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\" ")
				.append("xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" ")
				.append("xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\">");
		element(xml, "cbc:CustomizationID", "fiscal-saas-preprod-b2b");
		element(xml, "cbc:ProfileID", "B2B_PREPARATION");
		element(xml, "cbc:ID", invoice.invoiceNumber());
		element(xml, "cbc:IssueDate", invoice.issueDate().toString());
		element(xml, "cbc:InvoiceTypeCode", invoice.invoiceType());
		element(xml, "cbc:DocumentCurrencyCode", invoice.currency());
		party(xml, "cac:AccountingSupplierParty", invoice.issuerCompany().legalName(), invoice.issuerCompany().taxId());
		party(xml, "cac:AccountingCustomerParty", invoice.customerCompany().legalName(), invoice.customerCompany().taxId());
		taxTotal(xml, invoice.currency(), taxes);
		xml.append("<cac:LegalMonetaryTotal>");
		amount(xml, "cbc:TaxExclusiveAmount", invoice.currency(), invoice.taxableBase());
		amount(xml, "cbc:TaxInclusiveAmount", invoice.currency(), invoice.total());
		amount(xml, "cbc:PayableAmount", invoice.currency(), invoice.total());
		xml.append("</cac:LegalMonetaryTotal>");
		for (FiscalInvoiceLine line : lines) {
			invoiceLine(xml, invoice.currency(), line);
		}
		xml.append("</ubl:Invoice>");
		return xml.toString();
	}

	private void party(StringBuilder xml, String container, String legalName, String taxId) {
		xml.append('<').append(container).append('>');
		xml.append("<cac:Party>");
		xml.append("<cac:PartyName>");
		element(xml, "cbc:Name", legalName);
		xml.append("</cac:PartyName>");
		xml.append("<cac:PartyTaxScheme>");
		element(xml, "cbc:CompanyID", taxId);
		xml.append("<cac:TaxScheme>");
		element(xml, "cbc:ID", "VAT");
		xml.append("</cac:TaxScheme>");
		xml.append("</cac:PartyTaxScheme>");
		xml.append("</cac:Party>");
		xml.append("</").append(container).append('>');
	}

	private void taxTotal(StringBuilder xml, String currency, List<FiscalInvoiceTax> taxes) {
		xml.append("<cac:TaxTotal>");
		BigDecimal total = taxes.stream().map(FiscalInvoiceTax::taxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
		amount(xml, "cbc:TaxAmount", currency, total);
		for (FiscalInvoiceTax tax : taxes) {
			xml.append("<cac:TaxSubtotal>");
			amount(xml, "cbc:TaxableAmount", currency, tax.taxableBase());
			amount(xml, "cbc:TaxAmount", currency, tax.taxAmount());
			xml.append("<cac:TaxCategory>");
			element(xml, "cbc:Percent", tax.taxRate().toPlainString());
			xml.append("<cac:TaxScheme>");
			element(xml, "cbc:ID", "VAT");
			xml.append("</cac:TaxScheme>");
			xml.append("</cac:TaxCategory>");
			xml.append("</cac:TaxSubtotal>");
		}
		xml.append("</cac:TaxTotal>");
	}

	private void invoiceLine(StringBuilder xml, String currency, FiscalInvoiceLine line) {
		xml.append("<cac:InvoiceLine>");
		element(xml, "cbc:ID", Integer.toString(line.lineNumber()));
		element(xml, "cbc:InvoicedQuantity", line.quantity().toPlainString());
		amount(xml, "cbc:LineExtensionAmount", currency, line.lineBase());
		xml.append("<cac:Item>");
		element(xml, "cbc:Description", line.description());
		xml.append("</cac:Item>");
		xml.append("<cac:Price>");
		amount(xml, "cbc:PriceAmount", currency, line.unitPrice());
		xml.append("</cac:Price>");
		xml.append("</cac:InvoiceLine>");
	}

	private void amount(StringBuilder xml, String name, String currency, BigDecimal value) {
		xml.append('<').append(name).append(" currencyID=\"").append(XmlEscaper.escape(currency)).append("\">")
				.append(value.toPlainString())
				.append("</").append(name).append('>');
	}

	private void element(StringBuilder xml, String name, String value) {
		xml.append('<').append(name).append('>')
				.append(XmlEscaper.escape(value))
				.append("</").append(name).append('>');
	}
}

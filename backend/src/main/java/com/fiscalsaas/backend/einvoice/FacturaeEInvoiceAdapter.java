package com.fiscalsaas.backend.einvoice;

import java.util.List;

import com.fiscalsaas.backend.invoices.FiscalInvoice;
import com.fiscalsaas.backend.invoices.FiscalInvoiceLine;
import com.fiscalsaas.backend.invoices.FiscalInvoiceTax;

import org.springframework.stereotype.Component;

@Component
class FacturaeEInvoiceAdapter implements EInvoiceAdapter {

	@Override
	public EInvoiceSyntax syntax() {
		return EInvoiceSyntax.FACTURAE;
	}

	@Override
	public String render(FiscalInvoice invoice, List<FiscalInvoiceLine> lines, List<FiscalInvoiceTax> taxes) {
		StringBuilder xml = new StringBuilder();
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		xml.append("<Facturae>");
		xml.append("<FileHeader>");
		element(xml, "SchemaVersion", "3.2.2");
		element(xml, "Modality", "I");
		element(xml, "InvoiceIssuerType", "EM");
		xml.append("</FileHeader>");
		xml.append("<Parties>");
		party(xml, "SellerParty", invoice.issuerCompany().legalName(), invoice.issuerCompany().taxId());
		party(xml, "BuyerParty", invoice.customerCompany().legalName(), invoice.customerCompany().taxId());
		xml.append("</Parties>");
		xml.append("<Invoices><Invoice>");
		xml.append("<InvoiceHeader>");
		element(xml, "InvoiceNumber", invoice.invoiceNumber());
		element(xml, "InvoiceDocumentType", invoice.invoiceType());
		element(xml, "InvoiceClass", "OO");
		xml.append("</InvoiceHeader>");
		xml.append("<InvoiceIssueData>");
		element(xml, "IssueDate", invoice.issueDate().toString());
		element(xml, "InvoiceCurrencyCode", invoice.currency());
		xml.append("</InvoiceIssueData>");
		xml.append("<TaxesOutputs>");
		for (FiscalInvoiceTax tax : taxes) {
			xml.append("<Tax>");
			element(xml, "TaxTypeCode", "01");
			element(xml, "TaxRate", tax.taxRate().toPlainString());
			element(xml, "TaxableBase", tax.taxableBase().toPlainString());
			element(xml, "TaxAmount", tax.taxAmount().toPlainString());
			xml.append("</Tax>");
		}
		xml.append("</TaxesOutputs>");
		xml.append("<InvoiceTotals>");
		element(xml, "TotalGrossAmount", invoice.taxableBase().toPlainString());
		element(xml, "TotalTaxOutputs", invoice.taxTotal().toPlainString());
		element(xml, "InvoiceTotal", invoice.total().toPlainString());
		xml.append("</InvoiceTotals>");
		xml.append("<Items>");
		for (FiscalInvoiceLine line : lines) {
			xml.append("<InvoiceLine>");
			element(xml, "ItemDescription", line.description());
			element(xml, "Quantity", line.quantity().toPlainString());
			element(xml, "UnitPriceWithoutTax", line.unitPrice().toPlainString());
			element(xml, "TotalCost", line.lineBase().toPlainString());
			xml.append("</InvoiceLine>");
		}
		xml.append("</Items>");
		xml.append("</Invoice></Invoices>");
		xml.append("</Facturae>");
		return xml.toString();
	}

	private void party(StringBuilder xml, String container, String legalName, String taxId) {
		xml.append('<').append(container).append('>');
		xml.append("<TaxIdentification>");
		element(xml, "PersonTypeCode", "J");
		element(xml, "ResidenceTypeCode", "R");
		element(xml, "TaxIdentificationNumber", taxId);
		xml.append("</TaxIdentification>");
		xml.append("<LegalEntity>");
		element(xml, "CorporateName", legalName);
		xml.append("</LegalEntity>");
		xml.append("</").append(container).append('>');
	}

	private void element(StringBuilder xml, String name, String value) {
		xml.append('<').append(name).append('>')
				.append(XmlEscaper.escape(value))
				.append("</").append(name).append('>');
	}
}

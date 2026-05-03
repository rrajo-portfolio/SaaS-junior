package com.fiscalsaas.backend.invoices;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "fiscal_invoice_taxes")
public class FiscalInvoiceTax {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invoice_id", nullable = false)
	private FiscalInvoice invoice;

	@Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
	private BigDecimal taxRate;

	@Column(name = "taxable_base", nullable = false, precision = 19, scale = 2)
	private BigDecimal taxableBase;

	@Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal taxAmount;

	protected FiscalInvoiceTax() {
	}

	public static FiscalInvoiceTax create(FiscalInvoice invoice, BigDecimal taxRate, BigDecimal taxableBase, BigDecimal taxAmount) {
		FiscalInvoiceTax tax = new FiscalInvoiceTax();
		tax.id = UUID.randomUUID().toString();
		tax.invoice = invoice;
		tax.taxRate = taxRate;
		tax.taxableBase = taxableBase;
		tax.taxAmount = taxAmount;
		return tax;
	}

	public BigDecimal taxRate() {
		return taxRate;
	}

	public BigDecimal taxableBase() {
		return taxableBase;
	}

	public BigDecimal taxAmount() {
		return taxAmount;
	}
}

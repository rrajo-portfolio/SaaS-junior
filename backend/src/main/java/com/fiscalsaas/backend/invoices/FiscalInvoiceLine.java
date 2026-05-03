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
@Table(name = "fiscal_invoice_lines")
public class FiscalInvoiceLine {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invoice_id", nullable = false)
	private FiscalInvoice invoice;

	@Column(name = "line_number", nullable = false)
	private int lineNumber;

	@Column(nullable = false, length = 500)
	private String description;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal quantity;

	@Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
	private BigDecimal unitPrice;

	@Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
	private BigDecimal taxRate;

	@Column(name = "line_base", nullable = false, precision = 19, scale = 2)
	private BigDecimal lineBase;

	@Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal taxAmount;

	@Column(name = "line_total", nullable = false, precision = 19, scale = 2)
	private BigDecimal lineTotal;

	protected FiscalInvoiceLine() {
	}

	public static FiscalInvoiceLine create(
			FiscalInvoice invoice,
			int lineNumber,
			String description,
			BigDecimal quantity,
			BigDecimal unitPrice,
			BigDecimal taxRate,
			BigDecimal lineBase,
			BigDecimal taxAmount,
			BigDecimal lineTotal) {
		FiscalInvoiceLine line = new FiscalInvoiceLine();
		line.id = UUID.randomUUID().toString();
		line.invoice = invoice;
		line.lineNumber = lineNumber;
		line.description = description.trim();
		line.quantity = quantity;
		line.unitPrice = unitPrice;
		line.taxRate = taxRate;
		line.lineBase = lineBase;
		line.taxAmount = taxAmount;
		line.lineTotal = lineTotal;
		return line;
	}

	public int lineNumber() {
		return lineNumber;
	}

	public String description() {
		return description;
	}

	public BigDecimal quantity() {
		return quantity;
	}

	public BigDecimal unitPrice() {
		return unitPrice;
	}

	public BigDecimal taxRate() {
		return taxRate;
	}

	public BigDecimal lineBase() {
		return lineBase;
	}

	public BigDecimal taxAmount() {
		return taxAmount;
	}

	public BigDecimal lineTotal() {
		return lineTotal;
	}
}

package com.fiscalsaas.backend.invoices;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.fiscalsaas.backend.identity.Company;
import com.fiscalsaas.backend.identity.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "fiscal_invoices")
public class FiscalInvoice {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "issuer_company_id", nullable = false)
	private Company issuerCompany;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_company_id", nullable = false)
	private Company customerCompany;

	@Column(name = "invoice_number", nullable = false, length = 80)
	private String invoiceNumber;

	@Column(name = "invoice_type", nullable = false, length = 40)
	private String invoiceType;

	@Column(nullable = false, length = 30)
	private String status;

	@Column(name = "issue_date", nullable = false)
	private LocalDate issueDate;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "taxable_base", nullable = false, precision = 19, scale = 2)
	private BigDecimal taxableBase;

	@Column(name = "tax_total", nullable = false, precision = 19, scale = 2)
	private BigDecimal taxTotal;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal total;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rectifies_invoice_id")
	private FiscalInvoice rectifiesInvoice;

	@Column(name = "created_by_user_id", nullable = false, length = 36)
	private String createdByUserId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected FiscalInvoice() {
	}

	public static FiscalInvoice create(
			Tenant tenant,
			Company issuerCompany,
			Company customerCompany,
			String invoiceNumber,
			InvoiceType invoiceType,
			LocalDate issueDate,
			String currency,
			BigDecimal taxableBase,
			BigDecimal taxTotal,
			BigDecimal total,
			FiscalInvoice rectifiesInvoice,
			String userId) {
		Instant now = Instant.now();
		FiscalInvoice invoice = new FiscalInvoice();
		invoice.id = UUID.randomUUID().toString();
		invoice.tenant = tenant;
		invoice.issuerCompany = issuerCompany;
		invoice.customerCompany = customerCompany;
		invoice.invoiceNumber = invoiceNumber.trim();
		invoice.invoiceType = invoiceType.name();
		invoice.status = InvoiceStatus.DRAFT.name();
		invoice.issueDate = issueDate;
		invoice.currency = currency.trim().toUpperCase();
		invoice.taxableBase = taxableBase;
		invoice.taxTotal = taxTotal;
		invoice.total = total;
		invoice.rectifiesInvoice = rectifiesInvoice;
		invoice.createdByUserId = userId;
		invoice.createdAt = now;
		invoice.updatedAt = now;
		return invoice;
	}

	public void updateStatus(InvoiceStatus status) {
		this.status = status.name();
		this.updatedAt = Instant.now();
	}

	public String id() {
		return id;
	}

	public String tenantId() {
		return tenant.id();
	}

	public Tenant tenant() {
		return tenant;
	}

	public Company issuerCompany() {
		return issuerCompany;
	}

	public Company customerCompany() {
		return customerCompany;
	}

	public String invoiceNumber() {
		return invoiceNumber;
	}

	public String invoiceType() {
		return invoiceType;
	}

	public String status() {
		return status;
	}

	public LocalDate issueDate() {
		return issueDate;
	}

	public String currency() {
		return currency;
	}

	public BigDecimal taxableBase() {
		return taxableBase;
	}

	public BigDecimal taxTotal() {
		return taxTotal;
	}

	public BigDecimal total() {
		return total;
	}

	public String rectifiesInvoiceId() {
		return rectifiesInvoice == null ? null : rectifiesInvoice.id();
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}

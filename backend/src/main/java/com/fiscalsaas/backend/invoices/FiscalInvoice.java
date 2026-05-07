package com.fiscalsaas.backend.invoices;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.fiscalsaas.backend.identity.Company;
import com.fiscalsaas.backend.identity.Tenant;
import com.fiscalsaas.backend.fiscal.Customer;
import com.fiscalsaas.backend.fiscal.InvoiceSeries;

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@Column(name = "invoice_number", nullable = false, length = 80)
	private String invoiceNumber;

	@Column(name = "invoice_type", nullable = false, length = 40)
	private String invoiceType;

	@Column(nullable = false, length = 30)
	private String status;

	@Column(name = "issue_date", nullable = false)
	private LocalDate issueDate;

	@Column(name = "due_date")
	private LocalDate dueDate;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "taxable_base", nullable = false, precision = 19, scale = 2)
	private BigDecimal taxableBase;

	@Column(name = "tax_total", nullable = false, precision = 19, scale = 2)
	private BigDecimal taxTotal;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal total;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "series_id")
	private InvoiceSeries series;

	@Column(name = "series_code", length = 40)
	private String seriesCode;

	@Column(name = "fiscal_number", length = 80)
	private String fiscalNumber;

	@Column(name = "issued_at")
	private Instant issuedAt;

	@Column(name = "issue_request_id", length = 80)
	private String issueRequestId;

	@Column(name = "customer_snapshot", length = 2000)
	private String customerSnapshot;

	@Column(name = "issuer_fiscal_snapshot", length = 2000)
	private String issuerFiscalSnapshot;

	@Column(name = "payment_status", nullable = false, length = 30)
	private String paymentStatus;

	@Column(name = "paid_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal paidAmount;

	@Column(name = "outstanding_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal outstandingAmount;

	@Column(name = "withholding_total", nullable = false, precision = 19, scale = 2)
	private BigDecimal withholdingTotal;

	@Column(name = "gross_total", nullable = false, precision = 19, scale = 2)
	private BigDecimal grossTotal;

	@Column(name = "net_total", nullable = false, precision = 19, scale = 2)
	private BigDecimal netTotal;

	@Column(name = "payable_total", nullable = false, precision = 19, scale = 2)
	private BigDecimal payableTotal;

	@Column(name = "totals_snapshot", length = 2000)
	private String totalsSnapshot;

	@Column(name = "cancellation_reason", length = 500)
	private String cancellationReason;

	@Column(name = "cancelled_at")
	private Instant cancelledAt;

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
			Customer customer,
			String invoiceNumber,
			InvoiceType invoiceType,
			LocalDate issueDate,
			LocalDate dueDate,
			String currency,
			BigDecimal taxableBase,
			BigDecimal taxTotal,
			BigDecimal total,
			BigDecimal withholdingTotal,
			BigDecimal grossTotal,
			BigDecimal netTotal,
			FiscalInvoice rectifiesInvoice,
			String userId) {
		Instant now = Instant.now();
		FiscalInvoice invoice = new FiscalInvoice();
		invoice.id = UUID.randomUUID().toString();
		invoice.tenant = tenant;
		invoice.issuerCompany = issuerCompany;
		invoice.customerCompany = customerCompany;
		invoice.customer = customer;
		invoice.invoiceNumber = invoiceNumber.trim();
		invoice.invoiceType = invoiceType.name();
		invoice.status = InvoiceStatus.DRAFT.name();
		invoice.issueDate = issueDate;
		invoice.dueDate = dueDate;
		invoice.currency = currency.trim().toUpperCase();
		invoice.taxableBase = taxableBase;
		invoice.taxTotal = taxTotal;
		invoice.total = total;
		invoice.withholdingTotal = withholdingTotal;
		invoice.grossTotal = grossTotal;
		invoice.netTotal = netTotal;
		invoice.payableTotal = total;
		invoice.paymentStatus = "UNPAID";
		invoice.paidAmount = BigDecimal.ZERO;
		invoice.outstandingAmount = BigDecimal.ZERO;
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

	public void issue(
			InvoiceSeries series,
			String fiscalNumber,
			String issueRequestId,
			String issuerFiscalSnapshot,
			String customerSnapshot,
			String totalsSnapshot,
			Instant issuedAt) {
		this.series = series;
		this.seriesCode = series.code();
		this.fiscalNumber = fiscalNumber;
		this.issueRequestId = issueRequestId;
		this.issuerFiscalSnapshot = issuerFiscalSnapshot;
		this.customerSnapshot = customerSnapshot;
		this.totalsSnapshot = totalsSnapshot;
		this.issuedAt = issuedAt;
		this.status = InvoiceStatus.ISSUED.name();
		this.outstandingAmount = payableTotal.subtract(paidAmount);
		this.paymentStatus = outstandingAmount.compareTo(BigDecimal.ZERO) <= 0 ? "PAID" : "UNPAID";
		this.updatedAt = issuedAt;
	}

	public void cancelLocal(String reason) {
		this.status = InvoiceStatus.CANCELLED_LOCAL.name();
		this.cancellationReason = reason;
		this.cancelledAt = Instant.now();
		this.updatedAt = cancelledAt;
	}

	public void registerPayment(BigDecimal amount) {
		this.paidAmount = paidAmount.add(amount);
		this.outstandingAmount = payableTotal.subtract(paidAmount);
		if (outstandingAmount.compareTo(BigDecimal.ZERO) <= 0) {
			this.outstandingAmount = BigDecimal.ZERO;
			this.paymentStatus = "PAID";
		} else {
			this.paymentStatus = "PARTIALLY_PAID";
		}
		this.updatedAt = Instant.now();
	}

	public void updateDraft(
			Company issuerCompany,
			Company customerCompany,
			Customer customer,
			String invoiceNumber,
			InvoiceType invoiceType,
			LocalDate issueDate,
			LocalDate dueDate,
			String currency,
			BigDecimal taxableBase,
			BigDecimal taxTotal,
			BigDecimal total,
			BigDecimal withholdingTotal,
			BigDecimal grossTotal,
			BigDecimal netTotal,
			FiscalInvoice rectifiesInvoice) {
		this.issuerCompany = issuerCompany;
		this.customerCompany = customerCompany;
		this.customer = customer;
		this.invoiceNumber = invoiceNumber.trim();
		this.invoiceType = invoiceType.name();
		this.issueDate = issueDate;
		this.dueDate = dueDate;
		this.currency = currency.trim().toUpperCase();
		this.taxableBase = taxableBase;
		this.taxTotal = taxTotal;
		this.total = total;
		this.withholdingTotal = withholdingTotal;
		this.grossTotal = grossTotal;
		this.netTotal = netTotal;
		this.payableTotal = total;
		this.rectifiesInvoice = rectifiesInvoice;
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

	public Customer customer() {
		return customer;
	}

	public String customerId() {
		return customer == null ? null : customer.id();
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

	public LocalDate dueDate() {
		return dueDate;
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

	public String seriesId() {
		return series == null ? null : series.id();
	}

	public String seriesCode() {
		return seriesCode;
	}

	public String fiscalNumber() {
		return fiscalNumber;
	}

	public Instant issuedAt() {
		return issuedAt;
	}

	public String customerSnapshot() {
		return customerSnapshot;
	}

	public String issuerFiscalSnapshot() {
		return issuerFiscalSnapshot;
	}

	public String paymentStatus() {
		return paymentStatus;
	}

	public BigDecimal paidAmount() {
		return paidAmount;
	}

	public BigDecimal outstandingAmount() {
		return outstandingAmount;
	}

	public BigDecimal withholdingTotal() {
		return withholdingTotal;
	}

	public BigDecimal grossTotal() {
		return grossTotal;
	}

	public BigDecimal netTotal() {
		return netTotal;
	}

	public BigDecimal payableTotal() {
		return payableTotal;
	}

	public String totalsSnapshot() {
		return totalsSnapshot;
	}

	public String cancellationReason() {
		return cancellationReason;
	}

	public String rectifiesInvoiceId() {
		return rectifiesInvoice == null ? null : rectifiesInvoice.id();
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}

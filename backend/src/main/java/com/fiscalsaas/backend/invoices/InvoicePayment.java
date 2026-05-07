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
@Table(name = "invoice_payments")
public class InvoicePayment {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invoice_id", nullable = false)
	private FiscalInvoice invoice;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(name = "payment_date", nullable = false)
	private LocalDate paymentDate;

	@Column(nullable = false, length = 40)
	private String method;

	@Column(length = 120)
	private String reference;

	@Column(length = 500)
	private String notes;

	@Column(name = "created_by_user_id", nullable = false, length = 36)
	private String createdByUserId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected InvoicePayment() {
	}

	public static InvoicePayment create(
			Tenant tenant,
			Company company,
			FiscalInvoice invoice,
			BigDecimal amount,
			LocalDate paymentDate,
			String method,
			String reference,
			String notes,
			String userId) {
		InvoicePayment payment = new InvoicePayment();
		payment.id = UUID.randomUUID().toString();
		payment.tenant = tenant;
		payment.company = company;
		payment.invoice = invoice;
		payment.amount = amount;
		payment.paymentDate = paymentDate;
		payment.method = method;
		payment.reference = reference;
		payment.notes = notes;
		payment.createdByUserId = userId;
		payment.createdAt = Instant.now();
		return payment;
	}

	public String id() {
		return id;
	}

	public String tenantId() {
		return tenant.id();
	}

	public String companyId() {
		return company.id();
	}

	public String invoiceId() {
		return invoice.id();
	}

	public BigDecimal amount() {
		return amount;
	}

	public LocalDate paymentDate() {
		return paymentDate;
	}

	public String method() {
		return method;
	}

	public String reference() {
		return reference;
	}

	public String notes() {
		return notes;
	}

	public Instant createdAt() {
		return createdAt;
	}
}

package com.fiscalsaas.backend.einvoice;

import java.time.Instant;
import java.util.UUID;

import com.fiscalsaas.backend.identity.Tenant;
import com.fiscalsaas.backend.invoices.FiscalInvoice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "einvoice_messages")
public class EInvoiceMessage {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invoice_id", nullable = false)
	private FiscalInvoice invoice;

	@Column(nullable = false, length = 30)
	private String syntax;

	@Column(nullable = false, length = 30)
	private String direction;

	@Column(name = "exchange_status", nullable = false, length = 30)
	private String exchangeStatus;

	@Column(name = "commercial_status", nullable = false, length = 30)
	private String commercialStatus;

	@Column(name = "payment_status", nullable = false, length = 30)
	private String paymentStatus;

	@Column(nullable = false, length = 12000)
	private String payload;

	@Column(name = "payload_sha256", nullable = false, length = 64)
	private String payloadSha256;

	@Column(name = "status_reason", length = 500)
	private String statusReason;

	@Column(name = "created_by_user_id", nullable = false, length = 36)
	private String createdByUserId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected EInvoiceMessage() {
	}

	public static EInvoiceMessage create(
			Tenant tenant,
			FiscalInvoice invoice,
			EInvoiceSyntax syntax,
			EInvoiceDirection direction,
			String payload,
			String payloadSha256,
			String userId,
			Instant createdAt) {
		EInvoiceMessage message = new EInvoiceMessage();
		message.id = UUID.randomUUID().toString();
		message.tenant = tenant;
		message.invoice = invoice;
		message.syntax = syntax.name();
		message.direction = direction.name();
		message.exchangeStatus = EInvoiceExchangeStatus.GENERATED.name();
		message.commercialStatus = EInvoiceCommercialStatus.PENDING.name();
		message.paymentStatus = EInvoicePaymentStatus.UNPAID.name();
		message.payload = payload;
		message.payloadSha256 = payloadSha256;
		message.createdByUserId = userId;
		message.createdAt = createdAt;
		message.updatedAt = createdAt;
		return message;
	}

	public void updateExchangeStatus(EInvoiceExchangeStatus status) {
		this.exchangeStatus = status.name();
		this.updatedAt = Instant.now();
	}

	public void updateCommercialStatus(EInvoiceCommercialStatus status, String reason) {
		this.commercialStatus = status.name();
		this.statusReason = reason == null || reason.isBlank() ? null : reason.trim();
		this.updatedAt = Instant.now();
	}

	public void updatePaymentStatus(EInvoicePaymentStatus status) {
		this.paymentStatus = status.name();
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

	public FiscalInvoice invoice() {
		return invoice;
	}

	public String syntax() {
		return syntax;
	}

	public String direction() {
		return direction;
	}

	public String exchangeStatus() {
		return exchangeStatus;
	}

	public String commercialStatus() {
		return commercialStatus;
	}

	public String paymentStatus() {
		return paymentStatus;
	}

	public String payload() {
		return payload;
	}

	public String payloadSha256() {
		return payloadSha256;
	}

	public String statusReason() {
		return statusReason;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}

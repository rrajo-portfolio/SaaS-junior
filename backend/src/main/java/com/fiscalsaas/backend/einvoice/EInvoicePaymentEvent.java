package com.fiscalsaas.backend.einvoice;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fiscalsaas.backend.identity.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "einvoice_payment_events")
public class EInvoicePaymentEvent {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "message_id", nullable = false)
	private EInvoiceMessage message;

	@Column(name = "payment_status", nullable = false, length = 30)
	private String paymentStatus;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(name = "paid_at", nullable = false)
	private Instant paidAt;

	@Column(name = "payment_reference", length = 120)
	private String paymentReference;

	@Column(length = 500)
	private String notes;

	@Column(name = "created_by_user_id", nullable = false, length = 36)
	private String createdByUserId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected EInvoicePaymentEvent() {
	}

	public static EInvoicePaymentEvent create(
			Tenant tenant,
			EInvoiceMessage message,
			EInvoicePaymentStatus status,
			BigDecimal amount,
			Instant paidAt,
			String paymentReference,
			String notes,
			String userId,
			Instant createdAt) {
		EInvoicePaymentEvent event = new EInvoicePaymentEvent();
		event.id = UUID.randomUUID().toString();
		event.tenant = tenant;
		event.message = message;
		event.paymentStatus = status.name();
		event.amount = amount;
		event.paidAt = paidAt;
		event.paymentReference = normalize(paymentReference);
		event.notes = normalize(notes);
		event.createdByUserId = userId;
		event.createdAt = createdAt;
		return event;
	}

	private static String normalize(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	public String id() {
		return id;
	}

	public String messageId() {
		return message.id();
	}

	public String paymentStatus() {
		return paymentStatus;
	}

	public BigDecimal amount() {
		return amount;
	}

	public Instant paidAt() {
		return paidAt;
	}

	public String paymentReference() {
		return paymentReference;
	}

	public String notes() {
		return notes;
	}

	public Instant createdAt() {
		return createdAt;
	}
}

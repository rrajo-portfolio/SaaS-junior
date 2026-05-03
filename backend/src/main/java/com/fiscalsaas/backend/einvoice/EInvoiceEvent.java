package com.fiscalsaas.backend.einvoice;

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
@Table(name = "einvoice_events")
public class EInvoiceEvent {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "message_id", nullable = false)
	private EInvoiceMessage message;

	@Column(name = "event_type", nullable = false, length = 40)
	private String eventType;

	@Column(length = 2000)
	private String details;

	@Column(name = "event_at", nullable = false, updatable = false)
	private Instant eventAt;

	@Column(name = "created_by_user_id", nullable = false, length = 36)
	private String createdByUserId;

	protected EInvoiceEvent() {
	}

	public static EInvoiceEvent create(
			Tenant tenant,
			EInvoiceMessage message,
			EInvoiceEventType eventType,
			String details,
			String userId,
			Instant eventAt) {
		EInvoiceEvent event = new EInvoiceEvent();
		event.id = UUID.randomUUID().toString();
		event.tenant = tenant;
		event.message = message;
		event.eventType = eventType.name();
		event.details = details;
		event.createdByUserId = userId;
		event.eventAt = eventAt;
		return event;
	}

	public String id() {
		return id;
	}

	public String messageId() {
		return message.id();
	}

	public String eventType() {
		return eventType;
	}

	public String details() {
		return details;
	}

	public Instant eventAt() {
		return eventAt;
	}
}

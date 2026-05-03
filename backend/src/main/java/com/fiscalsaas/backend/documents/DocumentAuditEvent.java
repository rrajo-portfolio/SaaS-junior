package com.fiscalsaas.backend.documents;

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
@Table(name = "document_audit_events")
public class DocumentAuditEvent {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "document_id", nullable = false)
	private FiscalDocument document;

	@Column(name = "event_type", nullable = false, length = 60)
	private String eventType;

	@Column(name = "actor_user_id", nullable = false, length = 36)
	private String actorUserId;

	@Column(name = "event_at", nullable = false)
	private Instant eventAt;

	@Column(length = 600)
	private String details;

	protected DocumentAuditEvent() {
	}

	public static DocumentAuditEvent create(Tenant tenant, FiscalDocument document, String eventType, String actorUserId, String details) {
		DocumentAuditEvent event = new DocumentAuditEvent();
		event.id = UUID.randomUUID().toString();
		event.tenant = tenant;
		event.document = document;
		event.eventType = eventType;
		event.actorUserId = actorUserId;
		event.eventAt = Instant.now();
		event.details = details;
		return event;
	}

	public String eventType() {
		return eventType;
	}

	public Instant eventAt() {
		return eventAt;
	}

	public String details() {
		return details;
	}
}

package com.fiscalsaas.backend.verifactu;

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
@Table(name = "sif_event_log")
public class SifEventLog {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "record_id", nullable = false)
	private SifRecord record;

	@Column(name = "event_type", nullable = false, length = 60)
	private String eventType;

	@Column(name = "actor_user_id", nullable = false, length = 36)
	private String actorUserId;

	@Column(name = "event_at", nullable = false, updatable = false)
	private Instant eventAt;

	@Column(length = 600)
	private String details;

	protected SifEventLog() {
	}

	public static SifEventLog create(SifRecord record, SifEventType eventType, String actorUserId, String details, Instant eventAt) {
		SifEventLog event = new SifEventLog();
		event.id = UUID.randomUUID().toString();
		event.tenant = record.tenant();
		event.record = record;
		event.eventType = eventType.name();
		event.actorUserId = actorUserId;
		event.eventAt = eventAt;
		event.details = details;
		return event;
	}

	public String id() {
		return id;
	}

	public String recordId() {
		return record.id();
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

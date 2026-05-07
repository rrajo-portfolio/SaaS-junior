package com.fiscalsaas.backend.fiscal;

import java.time.Instant;
import java.util.UUID;

import com.fiscalsaas.backend.identity.AppUser;
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
@Table(name = "audit_events")
public class AuditEvent {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "actor_user_id", nullable = false)
	private AppUser actor;

	@Column(name = "event_type", nullable = false, length = 80)
	private String eventType;

	@Column(name = "entity_type", nullable = false, length = 60)
	private String entityType;

	@Column(name = "entity_id", nullable = false, length = 36)
	private String entityId;

	@Column(length = 2000)
	private String details;

	@Column(name = "previous_hash", nullable = false, length = 64)
	private String previousHash;

	@Column(name = "event_hash", nullable = false, length = 64)
	private String eventHash;

	@Column(name = "occurred_at", nullable = false, updatable = false)
	private Instant occurredAt;

	protected AuditEvent() {
	}

	public static AuditEvent create(
			Tenant tenant,
			Company company,
			AppUser actor,
			String eventType,
			String entityType,
			String entityId,
			String details,
			String previousHash,
			String eventHash,
			Instant occurredAt) {
		AuditEvent event = new AuditEvent();
		event.id = UUID.randomUUID().toString();
		event.tenant = tenant;
		event.company = company;
		event.actor = actor;
		event.eventType = eventType;
		event.entityType = entityType;
		event.entityId = entityId;
		event.details = details;
		event.previousHash = previousHash;
		event.eventHash = eventHash;
		event.occurredAt = occurredAt;
		return event;
	}

	public String id() {
		return id;
	}

	public String tenantId() {
		return tenant.id();
	}

	public String companyId() {
		return company == null ? null : company.id();
	}

	public String actorEmail() {
		return actor.email();
	}

	public String eventType() {
		return eventType;
	}

	public String entityType() {
		return entityType;
	}

	public String entityId() {
		return entityId;
	}

	public String details() {
		return details;
	}

	public String previousHash() {
		return previousHash;
	}

	public String eventHash() {
		return eventHash;
	}

	public Instant occurredAt() {
		return occurredAt;
	}
}

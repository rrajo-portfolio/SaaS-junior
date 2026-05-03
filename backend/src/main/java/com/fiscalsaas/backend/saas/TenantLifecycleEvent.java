package com.fiscalsaas.backend.saas;

import java.time.Instant;
import java.util.UUID;

import com.fiscalsaas.backend.identity.AppUser;
import com.fiscalsaas.backend.identity.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tenant_lifecycle_events")
public class TenantLifecycleEvent {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@Column(name = "event_type", nullable = false, length = 60)
	private String eventType;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "actor_user_id", nullable = false)
	private AppUser actor;

	@Column(name = "from_status", length = 30)
	private String fromStatus;

	@Column(name = "to_status", length = 30)
	private String toStatus;

	@Column(name = "from_plan_code", length = 40)
	private String fromPlanCode;

	@Column(name = "to_plan_code", length = 40)
	private String toPlanCode;

	@Column(length = 500)
	private String notes;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected TenantLifecycleEvent() {
	}

	public static TenantLifecycleEvent create(
			Tenant tenant,
			String eventType,
			AppUser actor,
			String fromStatus,
			String toStatus,
			String fromPlanCode,
			String toPlanCode,
			String notes) {
		TenantLifecycleEvent event = new TenantLifecycleEvent();
		event.id = UUID.randomUUID().toString();
		event.tenant = tenant;
		event.eventType = eventType;
		event.actor = actor;
		event.fromStatus = fromStatus;
		event.toStatus = toStatus;
		event.fromPlanCode = fromPlanCode;
		event.toPlanCode = toPlanCode;
		event.notes = notes;
		event.createdAt = Instant.now();
		return event;
	}

	public String id() {
		return id;
	}

	public String tenantId() {
		return tenant.id();
	}

	public String eventType() {
		return eventType;
	}

	public String actorEmail() {
		return actor.email();
	}

	public String fromStatus() {
		return fromStatus;
	}

	public String toStatus() {
		return toStatus;
	}

	public String fromPlanCode() {
		return fromPlanCode;
	}

	public String toPlanCode() {
		return toPlanCode;
	}

	public String notes() {
		return notes;
	}

	public Instant createdAt() {
		return createdAt;
	}
}

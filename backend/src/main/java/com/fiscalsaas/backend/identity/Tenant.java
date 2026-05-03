package com.fiscalsaas.backend.identity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tenants")
public class Tenant {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@Column(nullable = false, unique = true, length = 80)
	private String slug;

	@Column(name = "display_name", nullable = false, length = 180)
	private String displayName;

	@Column(nullable = false, length = 30)
	private String status;

	@Column(name = "plan_code", nullable = false, length = 40)
	private String planCode;

	@Column(name = "subscription_status", nullable = false, length = 30)
	private String subscriptionStatus;

	@Column(name = "trial_ends_at")
	private Instant trialEndsAt;

	@Column(name = "suspended_at")
	private Instant suspendedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Tenant() {
	}

	public static Tenant create(String slug, String displayName, String planCode, Instant trialEndsAt) {
		Tenant tenant = new Tenant();
		tenant.id = UUID.randomUUID().toString();
		tenant.slug = slug.trim().toLowerCase();
		tenant.displayName = displayName.trim();
		tenant.status = "ACTIVE";
		tenant.planCode = planCode.trim().toLowerCase();
		tenant.subscriptionStatus = "trialing";
		tenant.trialEndsAt = trialEndsAt;
		tenant.createdAt = Instant.now();
		return tenant;
	}

	public void activate() {
		this.status = "ACTIVE";
		this.subscriptionStatus = "active";
		this.suspendedAt = null;
	}

	public void suspend() {
		this.status = "SUSPENDED";
		this.subscriptionStatus = "suspended";
		this.suspendedAt = Instant.now();
	}

	public void cancel() {
		this.status = "CANCELLED";
		this.subscriptionStatus = "cancelled";
		this.suspendedAt = Instant.now();
	}

	public void changePlan(String planCode) {
		this.planCode = planCode.trim().toLowerCase();
	}

	public String id() {
		return id;
	}

	public String slug() {
		return slug;
	}

	public String displayName() {
		return displayName;
	}

	public String status() {
		return status;
	}

	public String planCode() {
		return planCode;
	}

	public String subscriptionStatus() {
		return subscriptionStatus;
	}

	public Instant trialEndsAt() {
		return trialEndsAt;
	}

	public Instant suspendedAt() {
		return suspendedAt;
	}
}

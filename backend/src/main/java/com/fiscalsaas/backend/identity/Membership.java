package com.fiscalsaas.backend.identity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "memberships")
public class Membership {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Column(nullable = false, length = 60)
	private String role;

	@Column(nullable = false, length = 30)
	private String status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Membership() {
	}

	public static Membership create(Tenant tenant, AppUser user, FiscalRole role) {
		Membership membership = new Membership();
		membership.id = UUID.randomUUID().toString();
		membership.tenant = tenant;
		membership.user = user;
		membership.role = role.value();
		membership.status = "ACTIVE";
		membership.createdAt = Instant.now();
		return membership;
	}

	public String id() {
		return id;
	}

	public Tenant tenant() {
		return tenant;
	}

	public AppUser user() {
		return user;
	}

	public FiscalRole fiscalRole() {
		return FiscalRole.fromValue(role);
	}

	public String status() {
		return status;
	}
}

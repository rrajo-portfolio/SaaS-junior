package com.fiscalsaas.backend.identity;

import java.time.Instant;

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

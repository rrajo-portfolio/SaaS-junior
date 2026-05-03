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
@Table(name = "sif_system_declarations")
public class SifSystemDeclaration {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@Column(nullable = false, length = 30)
	private String status;

	@Column(nullable = false, length = 5000)
	private String payload;

	@Column(name = "payload_sha256", nullable = false, length = 64)
	private String payloadSha256;

	@Column(name = "created_by_user_id", nullable = false, length = 36)
	private String createdByUserId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected SifSystemDeclaration() {
	}

	public static SifSystemDeclaration create(Tenant tenant, String payload, String payloadSha256, String userId, Instant createdAt) {
		SifSystemDeclaration declaration = new SifSystemDeclaration();
		declaration.id = UUID.randomUUID().toString();
		declaration.tenant = tenant;
		declaration.status = "DRAFT";
		declaration.payload = payload;
		declaration.payloadSha256 = payloadSha256;
		declaration.createdByUserId = userId;
		declaration.createdAt = createdAt;
		return declaration;
	}

	public String id() {
		return id;
	}

	public String tenantId() {
		return tenant.id();
	}

	public String status() {
		return status;
	}

	public String payload() {
		return payload;
	}

	public String payloadSha256() {
		return payloadSha256;
	}

	public Instant createdAt() {
		return createdAt;
	}
}

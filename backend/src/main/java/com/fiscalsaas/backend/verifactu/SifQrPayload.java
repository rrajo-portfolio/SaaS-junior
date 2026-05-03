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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sif_qr_payloads")
public class SifQrPayload {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "record_id", nullable = false)
	private SifRecord record;

	@Column(name = "qr_payload", nullable = false, length = 1000)
	private String qrPayload;

	@Column(name = "qr_sha256", nullable = false, length = 64)
	private String qrSha256;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected SifQrPayload() {
	}

	public static SifQrPayload create(SifRecord record, String qrPayload, String qrSha256, Instant createdAt) {
		SifQrPayload payload = new SifQrPayload();
		payload.id = UUID.randomUUID().toString();
		payload.tenant = record.tenant();
		payload.record = record;
		payload.qrPayload = qrPayload;
		payload.qrSha256 = qrSha256;
		payload.createdAt = createdAt;
		return payload;
	}

	public String id() {
		return id;
	}

	public String tenantId() {
		return tenant.id();
	}

	public String recordId() {
		return record.id();
	}

	public String qrPayload() {
		return qrPayload;
	}

	public String qrSha256() {
		return qrSha256;
	}

	public Instant createdAt() {
		return createdAt;
	}
}

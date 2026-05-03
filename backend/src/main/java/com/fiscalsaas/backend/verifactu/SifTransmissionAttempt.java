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
@Table(name = "sif_transmission_attempts")
public class SifTransmissionAttempt {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "record_id", nullable = false)
	private SifRecord record;

	@Column(nullable = false, length = 30)
	private String mode;

	@Column(nullable = false, length = 40)
	private String status;

	@Column(name = "request_payload", nullable = false, length = 4000)
	private String requestPayload;

	@Column(name = "response_payload", nullable = false, length = 4000)
	private String responsePayload;

	@Column(name = "created_by_user_id", nullable = false, length = 36)
	private String createdByUserId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected SifTransmissionAttempt() {
	}

	public static SifTransmissionAttempt create(
			Tenant tenant,
			SifRecord record,
			AeatTransmissionMode mode,
			SifTransmissionStatus status,
			String requestPayload,
			String responsePayload,
			String userId,
			Instant createdAt) {
		SifTransmissionAttempt attempt = new SifTransmissionAttempt();
		attempt.id = UUID.randomUUID().toString();
		attempt.tenant = tenant;
		attempt.record = record;
		attempt.mode = mode.name();
		attempt.status = status.name();
		attempt.requestPayload = requestPayload;
		attempt.responsePayload = responsePayload;
		attempt.createdByUserId = userId;
		attempt.createdAt = createdAt;
		return attempt;
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

	public String mode() {
		return mode;
	}

	public String status() {
		return status;
	}

	public String requestPayload() {
		return requestPayload;
	}

	public String responsePayload() {
		return responsePayload;
	}

	public Instant createdAt() {
		return createdAt;
	}
}

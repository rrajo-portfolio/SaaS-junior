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
@Table(name = "sif_export_batches")
public class SifExportBatch {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@Column(name = "record_from_sequence", nullable = false)
	private long recordFromSequence;

	@Column(name = "record_to_sequence", nullable = false)
	private long recordToSequence;

	@Column(name = "record_count", nullable = false)
	private int recordCount;

	@Column(name = "export_sha256", nullable = false, length = 64)
	private String exportSha256;

	@Column(nullable = false, length = 10000)
	private String payload;

	@Column(name = "created_by_user_id", nullable = false, length = 36)
	private String createdByUserId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected SifExportBatch() {
	}

	public static SifExportBatch create(
			Tenant tenant,
			long recordFromSequence,
			long recordToSequence,
			int recordCount,
			String exportSha256,
			String payload,
			String userId,
			Instant createdAt) {
		SifExportBatch batch = new SifExportBatch();
		batch.id = UUID.randomUUID().toString();
		batch.tenant = tenant;
		batch.recordFromSequence = recordFromSequence;
		batch.recordToSequence = recordToSequence;
		batch.recordCount = recordCount;
		batch.exportSha256 = exportSha256;
		batch.payload = payload;
		batch.createdByUserId = userId;
		batch.createdAt = createdAt;
		return batch;
	}

	public String id() {
		return id;
	}

	public String tenantId() {
		return tenant.id();
	}

	public long recordFromSequence() {
		return recordFromSequence;
	}

	public long recordToSequence() {
		return recordToSequence;
	}

	public int recordCount() {
		return recordCount;
	}

	public String exportSha256() {
		return exportSha256;
	}

	public String payload() {
		return payload;
	}

	public Instant createdAt() {
		return createdAt;
	}
}

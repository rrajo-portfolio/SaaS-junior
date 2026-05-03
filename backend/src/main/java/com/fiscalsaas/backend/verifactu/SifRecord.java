package com.fiscalsaas.backend.verifactu;

import java.time.Instant;
import java.util.UUID;

import com.fiscalsaas.backend.identity.Tenant;
import com.fiscalsaas.backend.invoices.FiscalInvoice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sif_records")
public class SifRecord {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invoice_id", nullable = false)
	private FiscalInvoice invoice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "source_record_id")
	private SifRecord sourceRecord;

	@Column(name = "record_type", nullable = false, length = 40)
	private String recordType;

	@Column(name = "sequence_number", nullable = false)
	private long sequenceNumber;

	@Column(name = "previous_hash", nullable = false, length = 64)
	private String previousHash;

	@Column(name = "record_hash", nullable = false, length = 64)
	private String recordHash;

	@Column(name = "canonical_payload", nullable = false, length = 4000)
	private String canonicalPayload;

	@Column(name = "system_version", nullable = false, length = 40)
	private String systemVersion;

	@Column(name = "normative_version", nullable = false, length = 80)
	private String normativeVersion;

	@Column(name = "created_by_user_id", nullable = false, length = 36)
	private String createdByUserId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected SifRecord() {
	}

	public static SifRecord create(
			Tenant tenant,
			FiscalInvoice invoice,
			SifRecord sourceRecord,
			SifRecordType recordType,
			long sequenceNumber,
			String previousHash,
			String recordHash,
			String canonicalPayload,
			String systemVersion,
			String normativeVersion,
			String userId,
			Instant createdAt) {
		SifRecord record = new SifRecord();
		record.id = UUID.randomUUID().toString();
		record.tenant = tenant;
		record.invoice = invoice;
		record.sourceRecord = sourceRecord;
		record.recordType = recordType.name();
		record.sequenceNumber = sequenceNumber;
		record.previousHash = previousHash;
		record.recordHash = recordHash;
		record.canonicalPayload = canonicalPayload;
		record.systemVersion = systemVersion;
		record.normativeVersion = normativeVersion;
		record.createdByUserId = userId;
		record.createdAt = createdAt;
		return record;
	}

	public String id() {
		return id;
	}

	public String tenantId() {
		return tenant.id();
	}

	public Tenant tenant() {
		return tenant;
	}

	public FiscalInvoice invoice() {
		return invoice;
	}

	public String invoiceId() {
		return invoice.id();
	}

	public String sourceRecordId() {
		return sourceRecord == null ? null : sourceRecord.id();
	}

	public SifRecord sourceRecord() {
		return sourceRecord;
	}

	public String recordType() {
		return recordType;
	}

	public long sequenceNumber() {
		return sequenceNumber;
	}

	public String previousHash() {
		return previousHash;
	}

	public String recordHash() {
		return recordHash;
	}

	public String canonicalPayload() {
		return canonicalPayload;
	}

	public String systemVersion() {
		return systemVersion;
	}

	public String normativeVersion() {
		return normativeVersion;
	}

	public Instant createdAt() {
		return createdAt;
	}
}

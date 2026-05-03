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
@Table(name = "sif_record_hash_chain")
public class SifRecordHashChain {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "record_id", nullable = false)
	private SifRecord record;

	@Column(name = "sequence_number", nullable = false)
	private long sequenceNumber;

	@Column(name = "previous_hash", nullable = false, length = 64)
	private String previousHash;

	@Column(name = "record_hash", nullable = false, length = 64)
	private String recordHash;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected SifRecordHashChain() {
	}

	public static SifRecordHashChain create(SifRecord record) {
		SifRecordHashChain chain = new SifRecordHashChain();
		chain.id = UUID.randomUUID().toString();
		chain.tenant = record.tenant();
		chain.record = record;
		chain.sequenceNumber = record.sequenceNumber();
		chain.previousHash = record.previousHash();
		chain.recordHash = record.recordHash();
		chain.createdAt = record.createdAt();
		return chain;
	}
}

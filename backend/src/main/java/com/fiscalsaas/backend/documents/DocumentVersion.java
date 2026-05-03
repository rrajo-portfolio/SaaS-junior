package com.fiscalsaas.backend.documents;

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
@Table(name = "document_versions")
public class DocumentVersion {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "document_id", nullable = false)
	private FiscalDocument document;

	@Column(name = "version_number", nullable = false)
	private int versionNumber;

	@Column(name = "original_filename", nullable = false, length = 255)
	private String originalFilename;

	@Column(name = "content_type", nullable = false, length = 120)
	private String contentType;

	@Column(name = "byte_size", nullable = false)
	private long byteSize;

	@Column(nullable = false, length = 64)
	private String sha256;

	@Column(name = "storage_key", nullable = false, length = 500)
	private String storageKey;

	@Column(name = "uploaded_by_user_id", nullable = false, length = 36)
	private String uploadedByUserId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected DocumentVersion() {
	}

	public static DocumentVersion create(
			FiscalDocument document,
			int versionNumber,
			String originalFilename,
			String contentType,
			long byteSize,
			String sha256,
			String storageKey,
			String uploadedByUserId) {
		DocumentVersion version = new DocumentVersion();
		version.id = UUID.randomUUID().toString();
		version.document = document;
		version.versionNumber = versionNumber;
		version.originalFilename = originalFilename;
		version.contentType = contentType;
		version.byteSize = byteSize;
		version.sha256 = sha256;
		version.storageKey = storageKey;
		version.uploadedByUserId = uploadedByUserId;
		version.createdAt = Instant.now();
		return version;
	}

	public String id() {
		return id;
	}

	public int versionNumber() {
		return versionNumber;
	}

	public String originalFilename() {
		return originalFilename;
	}

	public String contentType() {
		return contentType;
	}

	public long byteSize() {
		return byteSize;
	}

	public String sha256() {
		return sha256;
	}

	public String storageKey() {
		return storageKey;
	}

	public Instant createdAt() {
		return createdAt;
	}
}

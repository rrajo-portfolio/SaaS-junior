package com.fiscalsaas.backend.documents;

import java.time.Instant;
import java.util.UUID;

import com.fiscalsaas.backend.identity.Company;
import com.fiscalsaas.backend.identity.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "fiscal_documents")
public class FiscalDocument {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Column(name = "document_type", nullable = false, length = 60)
	private String documentType;

	@Column(nullable = false, length = 220)
	private String title;

	@Column(nullable = false, length = 30)
	private String status;

	@Column(name = "current_version", nullable = false)
	private int currentVersion;

	@Column(name = "created_by_user_id", nullable = false, length = 36)
	private String createdByUserId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected FiscalDocument() {
	}

	public static FiscalDocument create(Tenant tenant, Company company, String documentType, String title, String userId) {
		Instant now = Instant.now();
		FiscalDocument document = new FiscalDocument();
		document.id = UUID.randomUUID().toString();
		document.tenant = tenant;
		document.company = company;
		document.documentType = documentType;
		document.title = title.trim();
		document.status = "ACTIVE";
		document.currentVersion = 1;
		document.createdByUserId = userId;
		document.createdAt = now;
		document.updatedAt = now;
		return document;
	}

	public void addVersion() {
		currentVersion += 1;
		updatedAt = Instant.now();
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

	public Company company() {
		return company;
	}

	public String documentType() {
		return documentType;
	}

	public String title() {
		return title;
	}

	public String status() {
		return status;
	}

	public int currentVersion() {
		return currentVersion;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}

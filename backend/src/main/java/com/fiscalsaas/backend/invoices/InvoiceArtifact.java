package com.fiscalsaas.backend.invoices;

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
@Table(name = "invoice_artifacts")
public class InvoiceArtifact {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invoice_id", nullable = false)
	private FiscalInvoice invoice;

	@Column(name = "artifact_type", nullable = false, length = 30)
	private String artifactType;

	@Column(nullable = false, length = 220)
	private String filename;

	@Column(nullable = false, length = 64)
	private String sha256;

	@Column(name = "generated_by_user_id", nullable = false, length = 36)
	private String generatedByUserId;

	@Column(name = "generated_at", nullable = false, updatable = false)
	private Instant generatedAt;

	protected InvoiceArtifact() {
	}

	public static InvoiceArtifact create(Tenant tenant, Company company, FiscalInvoice invoice, String artifactType, String filename, String sha256, String userId) {
		InvoiceArtifact artifact = new InvoiceArtifact();
		artifact.id = UUID.randomUUID().toString();
		artifact.tenant = tenant;
		artifact.company = company;
		artifact.invoice = invoice;
		artifact.artifactType = artifactType;
		artifact.filename = filename;
		artifact.sha256 = sha256;
		artifact.generatedByUserId = userId;
		artifact.generatedAt = Instant.now();
		return artifact;
	}

	public String id() {
		return id;
	}
}

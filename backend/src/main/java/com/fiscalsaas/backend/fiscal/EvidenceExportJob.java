package com.fiscalsaas.backend.fiscal;

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
@Table(name = "export_jobs")
public class EvidenceExportJob {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Column(name = "requested_by_user_id", nullable = false, length = 36)
	private String requestedByUserId;

	@Column(nullable = false, length = 30)
	private String status;

	@Column(name = "export_type", nullable = false, length = 40)
	private String exportType;

	@Column(name = "filters_json", length = 2000)
	private String filtersJson;

	@Column(nullable = false, length = 64)
	private String sha256;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "completed_at")
	private Instant completedAt;

	protected EvidenceExportJob() {
	}

	public static EvidenceExportJob completed(Tenant tenant, Company company, String userId, String filtersJson, String sha256) {
		Instant now = Instant.now();
		EvidenceExportJob job = new EvidenceExportJob();
		job.id = UUID.randomUUID().toString();
		job.tenant = tenant;
		job.company = company;
		job.requestedByUserId = userId;
		job.status = "COMPLETED";
		job.exportType = "EVIDENCE_PACK";
		job.filtersJson = filtersJson;
		job.sha256 = sha256;
		job.createdAt = now;
		job.completedAt = now;
		return job;
	}

	public String id() {
		return id;
	}

	public String tenantId() {
		return tenant.id();
	}

	public Company company() {
		return company;
	}

	public String status() {
		return status;
	}

	public String exportType() {
		return exportType;
	}

	public String sha256() {
		return sha256;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant completedAt() {
		return completedAt;
	}
}

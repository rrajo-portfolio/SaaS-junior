package com.fiscalsaas.backend.identity;

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
@Table(name = "companies")
public class Company {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@Column(name = "legal_name", nullable = false, length = 220)
	private String legalName;

	@Column(name = "tax_id", nullable = false, length = 40)
	private String taxId;

	@Column(name = "country_code", nullable = false, length = 2)
	private String countryCode;

	@Column(name = "relationship_type", nullable = false, length = 40)
	private String relationshipType;

	@Column(nullable = false, length = 30)
	private String status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Company() {
	}

	public static Company create(Tenant tenant, String legalName, String taxId, String countryCode, String relationshipType) {
		Company company = new Company();
		company.id = UUID.randomUUID().toString();
		company.tenant = tenant;
		company.legalName = legalName.trim();
		company.taxId = taxId.trim().toUpperCase();
		company.countryCode = countryCode.trim().toUpperCase();
		company.relationshipType = relationshipType.trim().toUpperCase();
		company.status = "ACTIVE";
		company.createdAt = Instant.now();
		return company;
	}

	public void update(String legalName, String taxId, String countryCode, String relationshipType, String status) {
		this.legalName = legalName.trim();
		this.taxId = taxId.trim().toUpperCase();
		this.countryCode = countryCode.trim().toUpperCase();
		this.relationshipType = relationshipType.trim().toUpperCase();
		this.status = status.trim().toUpperCase();
	}

	public void deactivate() {
		this.status = "INACTIVE";
	}

	public String id() {
		return id;
	}

	public String tenantId() {
		return tenant.id();
	}

	public String legalName() {
		return legalName;
	}

	public String taxId() {
		return taxId;
	}

	public String countryCode() {
		return countryCode;
	}

	public String relationshipType() {
		return relationshipType;
	}

	public String status() {
		return status;
	}
}

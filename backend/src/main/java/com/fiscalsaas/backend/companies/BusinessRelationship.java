package com.fiscalsaas.backend.companies;

import java.time.Instant;
import java.time.LocalDate;
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
@Table(name = "business_relationships")
public class BusinessRelationship {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "source_company_id", nullable = false)
	private Company sourceCompany;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "target_company_id", nullable = false)
	private Company targetCompany;

	@Column(name = "relationship_kind", nullable = false, length = 40)
	private String relationshipKind;

	@Column(nullable = false, length = 30)
	private String status;

	@Column(length = 500)
	private String notes;

	@Column(name = "starts_at", nullable = false)
	private LocalDate startsAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected BusinessRelationship() {
	}

	public static BusinessRelationship create(
			Tenant tenant,
			Company sourceCompany,
			Company targetCompany,
			BusinessRelationshipKind relationshipKind,
			String notes) {
		BusinessRelationship relationship = new BusinessRelationship();
		relationship.id = UUID.randomUUID().toString();
		relationship.tenant = tenant;
		relationship.sourceCompany = sourceCompany;
		relationship.targetCompany = targetCompany;
		relationship.relationshipKind = relationshipKind.name();
		relationship.status = "ACTIVE";
		relationship.notes = sanitizeNotes(notes);
		relationship.startsAt = LocalDate.now();
		relationship.createdAt = Instant.now();
		return relationship;
	}

	public void update(BusinessRelationshipKind relationshipKind, String status, String notes) {
		this.relationshipKind = relationshipKind.name();
		this.status = status.trim().toUpperCase();
		this.notes = sanitizeNotes(notes);
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

	public Company sourceCompany() {
		return sourceCompany;
	}

	public Company targetCompany() {
		return targetCompany;
	}

	public String relationshipKind() {
		return relationshipKind;
	}

	public String status() {
		return status;
	}

	public String notes() {
		return notes;
	}

	public LocalDate startsAt() {
		return startsAt;
	}

	private static String sanitizeNotes(String notes) {
		if (notes == null || notes.isBlank()) {
			return null;
		}
		return notes.trim();
	}
}

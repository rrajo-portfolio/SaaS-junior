package com.fiscalsaas.backend.companies;

import java.time.LocalDate;

public record BusinessRelationshipResponse(
		String id,
		String tenantId,
		CompanyResponse sourceCompany,
		CompanyResponse targetCompany,
		String relationshipKind,
		String status,
		String notes,
		LocalDate startsAt) {
	public static BusinessRelationshipResponse from(BusinessRelationship relationship) {
		return new BusinessRelationshipResponse(
				relationship.id(),
				relationship.tenantId(),
				CompanyResponse.from(relationship.sourceCompany()),
				CompanyResponse.from(relationship.targetCompany()),
				relationship.relationshipKind(),
				relationship.status(),
				relationship.notes(),
				relationship.startsAt());
	}
}

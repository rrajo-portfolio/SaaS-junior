package com.fiscalsaas.backend.companies;

import com.fiscalsaas.backend.identity.Company;

public record CompanyResponse(
		String id,
		String tenantId,
		String legalName,
		String taxId,
		String countryCode,
		String relationshipType,
		String status) {
	public static CompanyResponse from(Company company) {
		return new CompanyResponse(
				company.id(),
				company.tenantId(),
				company.legalName(),
				company.taxId(),
				company.countryCode(),
				company.relationshipType(),
				company.status());
	}
}

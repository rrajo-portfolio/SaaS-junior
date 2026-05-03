package com.fiscalsaas.backend.companies;

import jakarta.validation.constraints.Size;

public record UpdateCompanyRequest(
		@Size(max = 220) String legalName,
		@Size(max = 40) String taxId,
		@Size(min = 2, max = 2) String countryCode,
		@Size(max = 40) String relationshipType,
		@Size(max = 30) String status) {
}

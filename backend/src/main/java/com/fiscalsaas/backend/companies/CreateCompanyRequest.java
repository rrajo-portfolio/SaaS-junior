package com.fiscalsaas.backend.companies;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
		@NotBlank @Size(max = 220) String legalName,
		@NotBlank @Size(max = 40) String taxId,
		@NotBlank @Size(min = 2, max = 2) String countryCode,
		@NotBlank @Size(max = 40) String relationshipType) {
}

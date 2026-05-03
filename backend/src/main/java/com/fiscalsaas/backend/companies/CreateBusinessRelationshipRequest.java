package com.fiscalsaas.backend.companies;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBusinessRelationshipRequest(
		@NotBlank @Size(max = 36) String sourceCompanyId,
		@NotBlank @Size(max = 36) String targetCompanyId,
		@NotBlank @Size(max = 40) String relationshipKind,
		@Size(max = 500) String notes) {
}

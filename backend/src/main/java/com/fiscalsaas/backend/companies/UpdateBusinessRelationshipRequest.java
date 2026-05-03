package com.fiscalsaas.backend.companies;

import jakarta.validation.constraints.Size;

public record UpdateBusinessRelationshipRequest(
		@Size(max = 40) String relationshipKind,
		@Size(max = 30) String status,
		@Size(max = 500) String notes) {
}

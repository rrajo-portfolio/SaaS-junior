package com.fiscalsaas.backend.saas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeTenantPlanRequest(
		@NotBlank @Size(max = 40) String planCode,
		@Size(max = 500) String notes) {
}

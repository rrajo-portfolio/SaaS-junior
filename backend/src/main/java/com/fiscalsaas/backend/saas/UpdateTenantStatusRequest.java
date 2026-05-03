package com.fiscalsaas.backend.saas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTenantStatusRequest(
		@NotBlank @Size(max = 30) String status,
		@Size(max = 500) String notes) {
}

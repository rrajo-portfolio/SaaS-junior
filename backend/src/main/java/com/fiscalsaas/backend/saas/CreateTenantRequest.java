package com.fiscalsaas.backend.saas;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(
		@NotBlank @Pattern(regexp = "^[a-z0-9][a-z0-9-]{2,78}$") String slug,
		@NotBlank @Size(max = 180) String displayName,
		@NotBlank @Size(max = 40) String planCode,
		@NotBlank @Email @Size(max = 180) String adminEmail,
		@NotBlank @Size(max = 180) String adminDisplayName) {
}

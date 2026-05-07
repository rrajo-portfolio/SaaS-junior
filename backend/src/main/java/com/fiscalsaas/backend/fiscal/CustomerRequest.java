package com.fiscalsaas.backend.fiscal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
		@Size(max = 30) String customerType,
		@NotBlank @Size(max = 220) String name,
		@NotBlank @Size(max = 40) String nif,
		@Size(max = 40) String vatNumber,
		@Email @Size(max = 180) String email,
		@Size(max = 40) String phone,
		@NotBlank @Size(max = 220) String addressLine1,
		@Size(max = 220) String addressLine2,
		@NotBlank @Size(max = 120) String city,
		@Size(max = 120) String province,
		@NotBlank @Size(max = 20) String postalCode,
		@NotBlank @Size(min = 2, max = 2) String country) {
}

package com.fiscalsaas.backend.companies;

import java.util.Arrays;

import com.fiscalsaas.backend.api.ApiValidationException;

public enum CompanyRelationshipType {
	OWNER,
	CLIENT,
	SUPPLIER;

	public static CompanyRelationshipType fromValue(String value) {
		if (value == null || value.isBlank()) {
			throw new ApiValidationException("relationshipType is required.");
		}
		return Arrays.stream(values())
				.filter(type -> type.name().equals(value.trim().toUpperCase()))
				.findFirst()
				.orElseThrow(() -> new ApiValidationException("Unsupported relationshipType."));
	}
}

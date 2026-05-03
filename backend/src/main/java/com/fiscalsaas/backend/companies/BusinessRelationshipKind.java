package com.fiscalsaas.backend.companies;

import java.util.Arrays;

import com.fiscalsaas.backend.api.ApiValidationException;

public enum BusinessRelationshipKind {
	CLIENT_MANAGEMENT,
	SUPPLIER_PORTAL,
	GROUP_COMPANY,
	ADVISORY,
	DOCUMENT_EXCHANGE;

	public static BusinessRelationshipKind fromValue(String value) {
		if (value == null || value.isBlank()) {
			throw new ApiValidationException("relationshipKind is required.");
		}
		return Arrays.stream(values())
				.filter(kind -> kind.name().equals(value.trim().toUpperCase()))
				.findFirst()
				.orElseThrow(() -> new ApiValidationException("Unsupported relationshipKind."));
	}
}

package com.fiscalsaas.backend.documents;

import java.util.Arrays;

import com.fiscalsaas.backend.api.ApiValidationException;

public enum DocumentType {
	INVOICE_ISSUED,
	INVOICE_RECEIVED,
	CONTRACT,
	CERTIFICATE,
	TAX_REPORT,
	EVIDENCE,
	OTHER;

	public static DocumentType fromValue(String value) {
		if (value == null || value.isBlank()) {
			throw new ApiValidationException("documentType is required.");
		}
		return Arrays.stream(values())
				.filter(type -> type.name().equals(value.trim().toUpperCase()))
				.findFirst()
				.orElseThrow(() -> new ApiValidationException("Unsupported documentType."));
	}
}

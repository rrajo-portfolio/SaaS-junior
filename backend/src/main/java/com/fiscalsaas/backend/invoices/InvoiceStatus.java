package com.fiscalsaas.backend.invoices;

import java.util.Arrays;

import com.fiscalsaas.backend.api.ApiValidationException;

public enum InvoiceStatus {
	DRAFT,
	ISSUED,
	RECTIFIED,
	CANCELLED;

	public static InvoiceStatus fromValue(String value) {
		if (value == null || value.isBlank()) {
			throw new ApiValidationException("status is required.");
		}
		return Arrays.stream(values())
				.filter(status -> status.name().equals(value.trim().toUpperCase()))
				.findFirst()
				.orElseThrow(() -> new ApiValidationException("Unsupported invoice status."));
	}
}

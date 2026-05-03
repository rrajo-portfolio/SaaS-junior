package com.fiscalsaas.backend.einvoice;

import java.util.Arrays;

import com.fiscalsaas.backend.api.ApiValidationException;

public enum EInvoiceCommercialStatus {
	PENDING,
	ACCEPTED,
	REJECTED;

	public static EInvoiceCommercialStatus fromValue(String value) {
		if (value == null || value.isBlank()) {
			throw new ApiValidationException("Commercial status is required.");
		}
		return Arrays.stream(values())
				.filter(status -> status.name().equals(value.trim().toUpperCase()))
				.findFirst()
				.orElseThrow(() -> new ApiValidationException("Unsupported e-invoice commercial status."));
	}
}

package com.fiscalsaas.backend.einvoice;

import java.util.Arrays;

import com.fiscalsaas.backend.api.ApiValidationException;

public enum EInvoiceExchangeStatus {
	GENERATED,
	SENT,
	RECEIVED;

	public static EInvoiceExchangeStatus fromValue(String value) {
		if (value == null || value.isBlank()) {
			throw new ApiValidationException("Exchange status is required.");
		}
		return Arrays.stream(values())
				.filter(status -> status.name().equals(value.trim().toUpperCase()))
				.findFirst()
				.orElseThrow(() -> new ApiValidationException("Unsupported e-invoice exchange status."));
	}
}

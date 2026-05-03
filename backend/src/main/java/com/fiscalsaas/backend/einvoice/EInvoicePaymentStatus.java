package com.fiscalsaas.backend.einvoice;

import java.util.Arrays;

import com.fiscalsaas.backend.api.ApiValidationException;

public enum EInvoicePaymentStatus {
	UNPAID,
	PARTIALLY_PAID,
	PAID;

	public static EInvoicePaymentStatus fromValue(String value) {
		if (value == null || value.isBlank()) {
			throw new ApiValidationException("Payment status is required.");
		}
		return Arrays.stream(values())
				.filter(status -> status.name().equals(value.trim().toUpperCase()))
				.findFirst()
				.orElseThrow(() -> new ApiValidationException("Unsupported e-invoice payment status."));
	}
}

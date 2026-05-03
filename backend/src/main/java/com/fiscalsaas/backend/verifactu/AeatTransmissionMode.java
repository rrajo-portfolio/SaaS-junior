package com.fiscalsaas.backend.verifactu;

import java.util.Arrays;

import com.fiscalsaas.backend.api.ApiValidationException;

public enum AeatTransmissionMode {
	STUB,
	SANDBOX,
	PRODUCTION;

	public static AeatTransmissionMode fromValue(String value, AeatTransmissionMode fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}
		return Arrays.stream(values())
				.filter(mode -> mode.name().equals(value.trim().toUpperCase()))
				.findFirst()
				.orElseThrow(() -> new ApiValidationException("Unsupported AEAT transmission mode."));
	}
}

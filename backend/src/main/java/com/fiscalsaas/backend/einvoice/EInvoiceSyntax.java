package com.fiscalsaas.backend.einvoice;

import java.util.Arrays;

import com.fiscalsaas.backend.api.ApiValidationException;

public enum EInvoiceSyntax {
	UBL,
	FACTURAE;

	public static EInvoiceSyntax fromValue(String value, EInvoiceSyntax fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}
		return Arrays.stream(values())
				.filter(syntax -> syntax.name().equals(value.trim().toUpperCase()))
				.findFirst()
				.orElseThrow(() -> new ApiValidationException("Unsupported e-invoice syntax."));
	}
}

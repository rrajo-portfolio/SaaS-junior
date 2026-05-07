package com.fiscalsaas.backend.invoices;

import java.util.Arrays;

import com.fiscalsaas.backend.api.ApiValidationException;

public enum InvoiceType {
	STANDARD,
	CORRECTIVE,
	ISSUED,
	RECEIVED,
	RECTIFYING;

	public static InvoiceType fromValue(String value) {
		if (value == null || value.isBlank()) {
			throw new ApiValidationException("invoiceType is required.");
		}
		return Arrays.stream(values())
				.filter(type -> type.name().equals(value.trim().toUpperCase()))
				.findFirst()
				.orElseThrow(() -> new ApiValidationException("Unsupported invoiceType."));
	}

	public boolean corrective() {
		return this == CORRECTIVE || this == RECTIFYING;
	}
}

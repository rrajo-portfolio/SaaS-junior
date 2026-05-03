package com.fiscalsaas.backend.security;

import java.util.Locale;

public enum AuthenticationMode {
	DEMO,
	OIDC;

	public static AuthenticationMode from(String value) {
		if (value == null || value.isBlank()) {
			return DEMO;
		}
		return switch (value.trim().toLowerCase(Locale.ROOT)) {
			case "demo" -> DEMO;
			case "oidc" -> OIDC;
			default -> throw new IllegalArgumentException("Unsupported authentication mode: " + value);
		};
	}
}

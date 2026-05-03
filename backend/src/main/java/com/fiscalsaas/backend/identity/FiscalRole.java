package com.fiscalsaas.backend.identity;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FiscalRole {
	PLATFORM_ADMIN("platform_admin"),
	TENANT_ADMIN("tenant_admin"),
	FISCAL_MANAGER("fiscal_manager"),
	ACCOUNTANT("accountant"),
	CLIENT_USER("client_user"),
	AUDITOR("auditor"),
	READONLY("readonly");

	private final String value;

	FiscalRole(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	public static FiscalRole fromValue(String value) {
		return Arrays.stream(values())
				.filter(role -> role.value.equals(value))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unsupported role: " + value));
	}
}

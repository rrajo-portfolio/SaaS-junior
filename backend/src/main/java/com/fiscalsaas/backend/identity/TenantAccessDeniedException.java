package com.fiscalsaas.backend.identity;

public class TenantAccessDeniedException extends RuntimeException {
	public TenantAccessDeniedException(String message) {
		super(message);
	}
}

package com.fiscalsaas.backend.api;

public class ApiConflictException extends RuntimeException {
	public ApiConflictException(String message) {
		super(message);
	}
}

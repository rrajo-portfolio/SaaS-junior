package com.fiscalsaas.backend.api;

import java.time.Instant;

import com.fiscalsaas.backend.identity.TenantAccessDeniedException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(TenantAccessDeniedException.class)
	ResponseEntity<ApiError> tenantAccessDenied(TenantAccessDeniedException exception) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(new ApiError("tenant_access_denied", exception.getMessage(), Instant.now()));
	}

	record ApiError(String code, String message, Instant timestamp) {
	}
}

package com.fiscalsaas.backend.api;

import java.time.Instant;

import com.fiscalsaas.backend.identity.TenantAccessDeniedException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(TenantAccessDeniedException.class)
	ResponseEntity<ApiError> tenantAccessDenied(TenantAccessDeniedException exception) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(new ApiError("tenant_access_denied", exception.getMessage(), Instant.now()));
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	ResponseEntity<ApiError> notFound(ResourceNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiError("not_found", exception.getMessage(), Instant.now()));
	}

	@ExceptionHandler(ApiConflictException.class)
	ResponseEntity<ApiError> conflict(ApiConflictException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(new ApiError("conflict", exception.getMessage(), Instant.now()));
	}

	@ExceptionHandler(ApiValidationException.class)
	ResponseEntity<ApiError> validation(ApiValidationException exception) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiError("validation_error", exception.getMessage(), Instant.now()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiError> beanValidation(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.findFirst()
				.map(error -> error.getField() + " " + error.getDefaultMessage())
				.orElse("Request validation failed.");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiError("validation_error", message, Instant.now()));
	}

	record ApiError(String code, String message, Instant timestamp) {
	}
}

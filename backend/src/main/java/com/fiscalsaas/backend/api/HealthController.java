package com.fiscalsaas.backend.api;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class HealthController {

	@GetMapping("/health")
	HealthResponse health() {
		return new HealthResponse("ok", "fiscal-saas-backend", Instant.now());
	}

	record HealthResponse(String status, String service, Instant checkedAt) {
	}
}


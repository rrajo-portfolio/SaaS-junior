package com.fiscalsaas.backend.verifactu;

import java.time.Instant;

public record SifTransmissionAttemptResponse(
		String id,
		String tenantId,
		String recordId,
		String mode,
		String status,
		String requestPayload,
		String responsePayload,
		Instant createdAt) {
	static SifTransmissionAttemptResponse from(SifTransmissionAttempt attempt) {
		return new SifTransmissionAttemptResponse(
				attempt.id(),
				attempt.tenantId(),
				attempt.recordId(),
				attempt.mode(),
				attempt.status(),
				attempt.requestPayload(),
				attempt.responsePayload(),
				attempt.createdAt());
	}
}

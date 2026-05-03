package com.fiscalsaas.backend.verifactu;

import java.time.Instant;

public record SifQrPayloadResponse(String id, String tenantId, String recordId, String qrPayload, String qrSha256, Instant createdAt) {
	static SifQrPayloadResponse from(SifQrPayload payload) {
		return new SifQrPayloadResponse(
				payload.id(),
				payload.tenantId(),
				payload.recordId(),
				payload.qrPayload(),
				payload.qrSha256(),
				payload.createdAt());
	}
}

package com.fiscalsaas.backend.verifactu;

import java.time.Instant;

public record SifSystemDeclarationResponse(
		String id,
		String tenantId,
		String status,
		String payload,
		String payloadSha256,
		Instant createdAt) {
	static SifSystemDeclarationResponse from(SifSystemDeclaration declaration) {
		return new SifSystemDeclarationResponse(
				declaration.id(),
				declaration.tenantId(),
				declaration.status(),
				declaration.payload(),
				declaration.payloadSha256(),
				declaration.createdAt());
	}
}

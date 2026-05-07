package com.fiscalsaas.backend.fiscal;

import java.time.Instant;

public record EvidenceExportResponse(
		String id,
		String tenantId,
		String companyId,
		String status,
		String exportType,
		String sha256,
		Instant createdAt,
		Instant completedAt) {
	static EvidenceExportResponse from(EvidenceExportJob job) {
		return new EvidenceExportResponse(
				job.id(),
				job.tenantId(),
				job.company().id(),
				job.status(),
				job.exportType(),
				job.sha256(),
				job.createdAt(),
				job.completedAt());
	}
}

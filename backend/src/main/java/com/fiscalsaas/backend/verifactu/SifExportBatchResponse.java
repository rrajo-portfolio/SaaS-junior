package com.fiscalsaas.backend.verifactu;

import java.time.Instant;

public record SifExportBatchResponse(
		String id,
		String tenantId,
		long recordFromSequence,
		long recordToSequence,
		int recordCount,
		String exportSha256,
		String payload,
		Instant createdAt) {
	static SifExportBatchResponse from(SifExportBatch batch) {
		return new SifExportBatchResponse(
				batch.id(),
				batch.tenantId(),
				batch.recordFromSequence(),
				batch.recordToSequence(),
				batch.recordCount(),
				batch.exportSha256(),
				batch.payload(),
				batch.createdAt());
	}
}

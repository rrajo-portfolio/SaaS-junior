package com.fiscalsaas.backend.documents;

import java.time.Instant;

import com.fiscalsaas.backend.companies.CompanyResponse;

public record DocumentResponse(
		String id,
		String tenantId,
		CompanyResponse company,
		String documentType,
		String title,
		String status,
		int currentVersion,
		String latestSha256,
		long latestByteSize,
		String latestFilename,
		Instant updatedAt) {
	static DocumentResponse from(FiscalDocument document, DocumentVersion latestVersion) {
		return new DocumentResponse(
				document.id(),
				document.tenantId(),
				CompanyResponse.from(document.company()),
				document.documentType(),
				document.title(),
				document.status(),
				document.currentVersion(),
				latestVersion.sha256(),
				latestVersion.byteSize(),
				latestVersion.originalFilename(),
				document.updatedAt());
	}
}

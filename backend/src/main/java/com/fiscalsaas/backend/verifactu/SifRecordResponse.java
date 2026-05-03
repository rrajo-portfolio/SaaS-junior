package com.fiscalsaas.backend.verifactu;

import java.time.Instant;

import com.fiscalsaas.backend.companies.CompanyResponse;

public record SifRecordResponse(
		String id,
		String tenantId,
		String invoiceId,
		String invoiceNumber,
		CompanyResponse issuerCompany,
		CompanyResponse customerCompany,
		String sourceRecordId,
		String recordType,
		long sequenceNumber,
		String previousHash,
		String recordHash,
		String canonicalPayload,
		String systemVersion,
		String normativeVersion,
		Instant createdAt) {
	static SifRecordResponse from(SifRecord record) {
		return new SifRecordResponse(
				record.id(),
				record.tenantId(),
				record.invoiceId(),
				record.invoice().invoiceNumber(),
				CompanyResponse.from(record.invoice().issuerCompany()),
				CompanyResponse.from(record.invoice().customerCompany()),
				record.sourceRecordId(),
				record.recordType(),
				record.sequenceNumber(),
				record.previousHash(),
				record.recordHash(),
				record.canonicalPayload(),
				record.systemVersion(),
				record.normativeVersion(),
				record.createdAt());
	}
}

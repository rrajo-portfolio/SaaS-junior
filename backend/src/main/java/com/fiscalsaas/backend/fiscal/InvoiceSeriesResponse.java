package com.fiscalsaas.backend.fiscal;

import java.time.Instant;

public record InvoiceSeriesResponse(
		String id,
		String tenantId,
		String companyId,
		String code,
		String prefix,
		long nextNumber,
		int padding,
		boolean active,
		Instant updatedAt) {
	static InvoiceSeriesResponse from(InvoiceSeries series) {
		return new InvoiceSeriesResponse(
				series.id(),
				series.tenantId(),
				series.company().id(),
				series.code(),
				series.prefix(),
				series.nextNumber(),
				series.padding(),
				series.active(),
				series.updatedAt());
	}
}

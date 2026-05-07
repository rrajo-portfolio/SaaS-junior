package com.fiscalsaas.backend.fiscal;

import java.math.BigDecimal;
import java.time.Instant;

public record FiscalSettingsResponse(
		String id,
		String tenantId,
		String companyId,
		String legalName,
		String tradeName,
		String nif,
		String vatNumber,
		String addressLine1,
		String addressLine2,
		String city,
		String province,
		String postalCode,
		String country,
		String defaultCurrency,
		int defaultPaymentTermsDays,
		BigDecimal defaultVatRate,
		String defaultLanguage,
		String pdfTemplate,
		String sifMode,
		boolean verifactuLabelEnabled,
		Instant updatedAt) {
	static FiscalSettingsResponse from(CompanyFiscalSettings settings) {
		return new FiscalSettingsResponse(
				settings.id(),
				settings.tenantId(),
				settings.company().id(),
				settings.legalName(),
				settings.tradeName(),
				settings.nif(),
				settings.vatNumber(),
				settings.addressLine1(),
				settings.addressLine2(),
				settings.city(),
				settings.province(),
				settings.postalCode(),
				settings.country(),
				settings.defaultCurrency(),
				settings.defaultPaymentTermsDays(),
				settings.defaultVatRate(),
				settings.defaultLanguage(),
				settings.pdfTemplate(),
				settings.sifMode(),
				settings.verifactuLabelEnabled(),
				settings.updatedAt());
	}
}

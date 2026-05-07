package com.fiscalsaas.backend.fiscal;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FiscalSettingsRequest(
		@NotBlank @Size(max = 220) String legalName,
		@Size(max = 220) String tradeName,
		@NotBlank @Size(max = 40) String nif,
		@Size(max = 40) String vatNumber,
		@NotBlank @Size(max = 220) String addressLine1,
		@Size(max = 220) String addressLine2,
		@NotBlank @Size(max = 120) String city,
		@Size(max = 120) String province,
		@NotBlank @Size(max = 20) String postalCode,
		@NotBlank @Size(min = 2, max = 2) String country,
		@NotBlank @Size(min = 3, max = 3) String defaultCurrency,
		@Min(0) @Max(365) Integer defaultPaymentTermsDays,
		@DecimalMin(value = "0.00") BigDecimal defaultVatRate,
		@Size(max = 8) String defaultLanguage,
		@Size(max = 80) String pdfTemplate,
		@Size(max = 30) String sifMode,
		Boolean verifactuLabelEnabled) {
}

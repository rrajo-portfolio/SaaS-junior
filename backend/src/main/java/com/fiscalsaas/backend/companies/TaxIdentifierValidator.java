package com.fiscalsaas.backend.companies;

import java.util.Locale;
import java.util.regex.Pattern;

import com.fiscalsaas.backend.api.ApiValidationException;

import org.springframework.stereotype.Component;

@Component
public class TaxIdentifierValidator {

	private static final Pattern ES_DNI = Pattern.compile("\\d{8}[TRWAGMYFPDXBNJZSQVHLCKE]");
	private static final Pattern ES_NIE = Pattern.compile("[XYZ]\\d{7}[TRWAGMYFPDXBNJZSQVHLCKE]");
	private static final Pattern ES_CIF = Pattern.compile("[ABCDEFGHJKLMNPQRSUVW]\\d{7}[0-9A-J]");
	private static final Pattern VAT = Pattern.compile("[A-Z]{2}[A-Z0-9]{2,18}");
	private static final String DNI_CONTROL = "TRWAGMYFPDXBNJZSQVHLCKE";

	ValidatedTaxId validate(String countryCode, String taxId) {
		String normalizedCountry = normalizeCountry(countryCode);
		String normalizedTaxId = normalizeTaxId(taxId);
		if ("ES".equals(normalizedCountry)) {
			String spanishTaxId = normalizedTaxId.startsWith("ES") ? normalizedTaxId.substring(2) : normalizedTaxId;
			if (!isValidSpanishTaxId(spanishTaxId)) {
				throw new ApiValidationException("taxId is not a valid basic ES NIF, NIE or CIF.");
			}
			return new ValidatedTaxId(normalizedCountry, spanishTaxId);
		}

		if (!VAT.matcher(normalizedTaxId).matches() || !normalizedTaxId.startsWith(normalizedCountry)) {
			throw new ApiValidationException("taxId must be a VAT-like identifier prefixed with countryCode.");
		}
		return new ValidatedTaxId(normalizedCountry, normalizedTaxId);
	}

	private String normalizeCountry(String countryCode) {
		if (countryCode == null || countryCode.isBlank()) {
			throw new ApiValidationException("countryCode is required.");
		}
		String normalized = countryCode.trim().toUpperCase(Locale.ROOT);
		if (!normalized.matches("[A-Z]{2}")) {
			throw new ApiValidationException("countryCode must use ISO alpha-2 format.");
		}
		return normalized;
	}

	private String normalizeTaxId(String taxId) {
		if (taxId == null || taxId.isBlank()) {
			throw new ApiValidationException("taxId is required.");
		}
		return taxId.replaceAll("[\\s.\\-]", "").toUpperCase(Locale.ROOT);
	}

	private boolean isValidSpanishTaxId(String taxId) {
		if (ES_CIF.matcher(taxId).matches()) {
			return true;
		}
		if (ES_DNI.matcher(taxId).matches()) {
			return hasValidDniControl(taxId);
		}
		if (ES_NIE.matcher(taxId).matches()) {
			String prefix = switch (taxId.charAt(0)) {
				case 'X' -> "0";
				case 'Y' -> "1";
				case 'Z' -> "2";
				default -> throw new IllegalStateException("Unexpected NIE prefix");
			};
			return hasValidDniControl(prefix + taxId.substring(1));
		}
		return false;
	}

	private boolean hasValidDniControl(String value) {
		int number = Integer.parseInt(value.substring(0, 8));
		char expected = DNI_CONTROL.charAt(number % 23);
		return value.charAt(8) == expected;
	}

	record ValidatedTaxId(String countryCode, String taxId) {
	}
}

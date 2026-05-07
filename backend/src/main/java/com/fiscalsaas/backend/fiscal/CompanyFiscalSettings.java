package com.fiscalsaas.backend.fiscal;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fiscalsaas.backend.identity.Company;
import com.fiscalsaas.backend.identity.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_fiscal_settings")
public class CompanyFiscalSettings {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Column(name = "legal_name", nullable = false, length = 220)
	private String legalName;

	@Column(name = "trade_name", length = 220)
	private String tradeName;

	@Column(nullable = false, length = 40)
	private String nif;

	@Column(name = "vat_number", length = 40)
	private String vatNumber;

	@Column(name = "address_line1", nullable = false, length = 220)
	private String addressLine1;

	@Column(name = "address_line2", length = 220)
	private String addressLine2;

	@Column(nullable = false, length = 120)
	private String city;

	@Column(length = 120)
	private String province;

	@Column(name = "postal_code", nullable = false, length = 20)
	private String postalCode;

	@Column(nullable = false, length = 2)
	private String country;

	@Column(name = "default_currency", nullable = false, length = 3)
	private String defaultCurrency;

	@Column(name = "default_payment_terms_days", nullable = false)
	private int defaultPaymentTermsDays;

	@Column(name = "default_vat_rate", nullable = false, precision = 5, scale = 2)
	private BigDecimal defaultVatRate;

	@Column(name = "default_language", nullable = false, length = 8)
	private String defaultLanguage;

	@Column(name = "pdf_template", nullable = false, length = 80)
	private String pdfTemplate;

	@Column(name = "sif_mode", nullable = false, length = 30)
	private String sifMode;

	@Column(name = "verifactu_label_enabled", nullable = false)
	private boolean verifactuLabelEnabled;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected CompanyFiscalSettings() {
	}

	public static CompanyFiscalSettings create(Tenant tenant, Company company, FiscalSettingsRequest request) {
		CompanyFiscalSettings settings = new CompanyFiscalSettings();
		settings.id = UUID.randomUUID().toString();
		settings.tenant = tenant;
		settings.company = company;
		settings.createdAt = Instant.now();
		settings.apply(request);
		return settings;
	}

	public void apply(FiscalSettingsRequest request) {
		this.legalName = required(request.legalName(), "legalName");
		this.tradeName = trimToNull(request.tradeName());
		this.nif = required(request.nif(), "nif").toUpperCase();
		this.vatNumber = trimToNull(request.vatNumber());
		this.addressLine1 = required(request.addressLine1(), "addressLine1");
		this.addressLine2 = trimToNull(request.addressLine2());
		this.city = required(request.city(), "city");
		this.province = trimToNull(request.province());
		this.postalCode = required(request.postalCode(), "postalCode");
		this.country = required(request.country(), "country").toUpperCase();
		this.defaultCurrency = required(request.defaultCurrency(), "defaultCurrency").toUpperCase();
		this.defaultPaymentTermsDays = request.defaultPaymentTermsDays() == null ? 30 : request.defaultPaymentTermsDays();
		this.defaultVatRate = request.defaultVatRate() == null ? new BigDecimal("21.00") : request.defaultVatRate();
		this.defaultLanguage = request.defaultLanguage() == null || request.defaultLanguage().isBlank() ? "es" : request.defaultLanguage().trim();
		this.pdfTemplate = request.pdfTemplate() == null || request.pdfTemplate().isBlank() ? "standard" : request.pdfTemplate().trim();
		this.sifMode = request.sifMode() == null || request.sifMode().isBlank() ? "LOCAL_ONLY" : request.sifMode().trim().toUpperCase();
		this.verifactuLabelEnabled = Boolean.TRUE.equals(request.verifactuLabelEnabled());
		this.updatedAt = Instant.now();
	}

	private static String required(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " is required.");
		}
		return value.trim();
	}

	private static String trimToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	public String id() {
		return id;
	}

	public String tenantId() {
		return tenant.id();
	}

	public Tenant tenant() {
		return tenant;
	}

	public Company company() {
		return company;
	}

	public String legalName() {
		return legalName;
	}

	public String tradeName() {
		return tradeName;
	}

	public String nif() {
		return nif;
	}

	public String vatNumber() {
		return vatNumber;
	}

	public String addressLine1() {
		return addressLine1;
	}

	public String addressLine2() {
		return addressLine2;
	}

	public String city() {
		return city;
	}

	public String province() {
		return province;
	}

	public String postalCode() {
		return postalCode;
	}

	public String country() {
		return country;
	}

	public String defaultCurrency() {
		return defaultCurrency;
	}

	public int defaultPaymentTermsDays() {
		return defaultPaymentTermsDays;
	}

	public BigDecimal defaultVatRate() {
		return defaultVatRate;
	}

	public String defaultLanguage() {
		return defaultLanguage;
	}

	public String pdfTemplate() {
		return pdfTemplate;
	}

	public String sifMode() {
		return sifMode;
	}

	public boolean verifactuLabelEnabled() {
		return verifactuLabelEnabled;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}

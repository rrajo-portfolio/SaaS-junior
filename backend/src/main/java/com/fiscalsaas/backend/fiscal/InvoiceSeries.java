package com.fiscalsaas.backend.fiscal;

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
@Table(name = "invoice_series")
public class InvoiceSeries {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Column(nullable = false, length = 40)
	private String code;

	@Column(nullable = false, length = 80)
	private String prefix;

	@Column(name = "next_number", nullable = false)
	private long nextNumber;

	@Column(nullable = false)
	private int padding;

	@Column(nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected InvoiceSeries() {
	}

	public static InvoiceSeries create(Tenant tenant, Company company, InvoiceSeriesRequest request) {
		InvoiceSeries series = new InvoiceSeries();
		series.id = UUID.randomUUID().toString();
		series.tenant = tenant;
		series.company = company;
		series.createdAt = Instant.now();
		series.apply(request);
		if (series.nextNumber < 1) {
			series.nextNumber = 1;
		}
		return series;
	}

	public void apply(InvoiceSeriesRequest request) {
		this.code = required(request.code(), "code").toUpperCase();
		this.prefix = required(request.prefix(), "prefix");
		this.nextNumber = request.nextNumber() == null ? Math.max(1, nextNumber) : request.nextNumber();
		this.padding = request.padding() == null ? 6 : request.padding();
		this.active = request.active() == null || request.active();
		this.updatedAt = Instant.now();
	}

	public String reserveNextFiscalNumber() {
		String number = prefix + String.format("%0" + padding + "d", nextNumber);
		this.nextNumber++;
		this.updatedAt = Instant.now();
		return number;
	}

	private static String required(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " is required.");
		}
		return value.trim();
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

	public String code() {
		return code;
	}

	public String prefix() {
		return prefix;
	}

	public long nextNumber() {
		return nextNumber;
	}

	public int padding() {
		return padding;
	}

	public boolean active() {
		return active;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}

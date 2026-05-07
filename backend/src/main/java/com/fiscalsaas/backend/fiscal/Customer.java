package com.fiscalsaas.backend.fiscal;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
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
@Table(name = "customers")
public class Customer {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Column(name = "customer_type", nullable = false, length = 30)
	private String customerType;

	@Column(nullable = false, length = 220)
	private String name;

	@Column(nullable = false, length = 40)
	private String nif;

	@Column(name = "vat_number", length = 40)
	private String vatNumber;

	@Column(length = 180)
	private String email;

	@Column(length = 40)
	private String phone;

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

	@Column(nullable = false, length = 30)
	private String status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Customer() {
	}

	public static Customer create(Tenant tenant, Company company, CustomerRequest request) {
		Customer customer = new Customer();
		customer.id = UUID.randomUUID().toString();
		customer.tenant = tenant;
		customer.company = company;
		customer.status = "ACTIVE";
		customer.createdAt = Instant.now();
		customer.apply(request);
		return customer;
	}

	public void apply(CustomerRequest request) {
		this.customerType = request.customerType() == null || request.customerType().isBlank()
				? "COMPANY"
				: request.customerType().trim().toUpperCase();
		this.name = required(request.name(), "name");
		this.nif = required(request.nif(), "nif").toUpperCase();
		this.vatNumber = trimToNull(request.vatNumber());
		this.email = trimToNull(request.email());
		this.phone = trimToNull(request.phone());
		this.addressLine1 = required(request.addressLine1(), "addressLine1");
		this.addressLine2 = trimToNull(request.addressLine2());
		this.city = required(request.city(), "city");
		this.province = trimToNull(request.province());
		this.postalCode = required(request.postalCode(), "postalCode");
		this.country = required(request.country(), "country").toUpperCase();
		this.updatedAt = Instant.now();
	}

	public void deactivate() {
		this.status = "INACTIVE";
		this.updatedAt = Instant.now();
	}

	public Map<String, Object> snapshot() {
		Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", id);
		snapshot.put("customerType", customerType);
		snapshot.put("name", name);
		snapshot.put("nif", nif);
		snapshot.put("vatNumber", vatNumber);
		snapshot.put("email", email);
		snapshot.put("addressLine1", addressLine1);
		snapshot.put("city", city);
		snapshot.put("province", province);
		snapshot.put("postalCode", postalCode);
		snapshot.put("country", country);
		return snapshot;
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

	public String customerType() {
		return customerType;
	}

	public String name() {
		return name;
	}

	public String nif() {
		return nif;
	}

	public String vatNumber() {
		return vatNumber;
	}

	public String email() {
		return email;
	}

	public String phone() {
		return phone;
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

	public String status() {
		return status;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}

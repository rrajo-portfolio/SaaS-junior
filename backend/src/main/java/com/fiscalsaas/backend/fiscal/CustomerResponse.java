package com.fiscalsaas.backend.fiscal;

import java.time.Instant;

public record CustomerResponse(
		String id,
		String tenantId,
		String companyId,
		String customerType,
		String name,
		String nif,
		String vatNumber,
		String email,
		String phone,
		String addressLine1,
		String addressLine2,
		String city,
		String province,
		String postalCode,
		String country,
		String status,
		Instant updatedAt) {
	static CustomerResponse from(Customer customer) {
		return new CustomerResponse(
				customer.id(),
				customer.tenantId(),
				customer.company().id(),
				customer.customerType(),
				customer.name(),
				customer.nif(),
				customer.vatNumber(),
				customer.email(),
				customer.phone(),
				customer.addressLine1(),
				customer.addressLine2(),
				customer.city(),
				customer.province(),
				customer.postalCode(),
				customer.country(),
				customer.status(),
				customer.updatedAt());
	}
}

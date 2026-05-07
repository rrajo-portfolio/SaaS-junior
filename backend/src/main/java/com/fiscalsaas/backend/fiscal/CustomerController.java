package com.fiscalsaas.backend.fiscal;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenants/{tenantId}/companies/{companyId}/customers")
public class CustomerController {

	private final CustomerService service;

	CustomerController(CustomerService service) {
		this.service = service;
	}

	@GetMapping
	List<CustomerResponse> listCustomers(
			@PathVariable String tenantId,
			@PathVariable String companyId,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String status,
			HttpServletRequest request) {
		return service.listCustomers(tenantId, companyId, search, status, request);
	}

	@GetMapping("/{customerId}")
	CustomerResponse getCustomer(
			@PathVariable String tenantId,
			@PathVariable String companyId,
			@PathVariable String customerId,
			HttpServletRequest request) {
		return service.getCustomer(tenantId, companyId, customerId, request);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	CustomerResponse createCustomer(
			@PathVariable String tenantId,
			@PathVariable String companyId,
			@Valid @RequestBody CustomerRequest body,
			HttpServletRequest request) {
		return service.createCustomer(tenantId, companyId, body, request);
	}

	@PutMapping("/{customerId}")
	CustomerResponse updateCustomer(
			@PathVariable String tenantId,
			@PathVariable String companyId,
			@PathVariable String customerId,
			@Valid @RequestBody CustomerRequest body,
			HttpServletRequest request) {
		return service.updateCustomer(tenantId, companyId, customerId, body, request);
	}

	@PatchMapping("/{customerId}/deactivate")
	CustomerResponse deactivateCustomer(
			@PathVariable String tenantId,
			@PathVariable String companyId,
			@PathVariable String customerId,
			HttpServletRequest request) {
		return service.deactivateCustomer(tenantId, companyId, customerId, request);
	}

	@DeleteMapping("/{customerId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void deleteCustomer(
			@PathVariable String tenantId,
			@PathVariable String companyId,
			@PathVariable String customerId,
			HttpServletRequest request) {
		service.deactivateCustomer(tenantId, companyId, customerId, request);
	}
}

package com.fiscalsaas.backend.saas;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform")
public class SaasAdminController {

	private final SaasAdminService service;

	SaasAdminController(SaasAdminService service) {
		this.service = service;
	}

	@GetMapping("/plans")
	List<SubscriptionPlanResponse> plans() {
		return service.listPlans();
	}

	@GetMapping("/tenants")
	List<TenantAdminResponse> tenants() {
		return service.listTenants();
	}

	@PostMapping("/tenants")
	@ResponseStatus(HttpStatus.CREATED)
	TenantAdminResponse createTenant(@Valid @RequestBody CreateTenantRequest request) {
		return service.createTenant(request);
	}

	@PatchMapping("/tenants/{tenantId}/status")
	TenantAdminResponse updateStatus(
			@PathVariable String tenantId,
			@Valid @RequestBody UpdateTenantStatusRequest request) {
		return service.updateStatus(tenantId, request);
	}

	@PatchMapping("/tenants/{tenantId}/plan")
	TenantAdminResponse changePlan(
			@PathVariable String tenantId,
			@Valid @RequestBody ChangeTenantPlanRequest request) {
		return service.changePlan(tenantId, request);
	}

	@GetMapping("/tenants/{tenantId}/events")
	List<TenantLifecycleEventResponse> events(@PathVariable String tenantId) {
		return service.listEvents(tenantId);
	}
}

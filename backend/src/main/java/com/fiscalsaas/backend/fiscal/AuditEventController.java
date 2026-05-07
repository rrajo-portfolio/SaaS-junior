package com.fiscalsaas.backend.fiscal;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenants/{tenantId}/companies/{companyId}/audit-events")
public class AuditEventController {

	private final AuditEventService service;

	AuditEventController(AuditEventService service) {
		this.service = service;
	}

	@GetMapping
	List<AuditEventResponse> listEvents(
			@PathVariable String tenantId,
			@PathVariable String companyId,
			HttpServletRequest request) {
		return service.listCompanyEvents(tenantId, companyId, request);
	}
}

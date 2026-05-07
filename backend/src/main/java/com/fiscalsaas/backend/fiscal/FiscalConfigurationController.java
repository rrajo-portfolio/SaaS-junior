package com.fiscalsaas.backend.fiscal;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenants/{tenantId}/companies/{companyId}")
public class FiscalConfigurationController {

	private final FiscalConfigurationService service;

	FiscalConfigurationController(FiscalConfigurationService service) {
		this.service = service;
	}

	@GetMapping("/fiscal-settings")
	FiscalSettingsResponse getSettings(
			@PathVariable String tenantId,
			@PathVariable String companyId,
			HttpServletRequest request) {
		return service.getSettings(tenantId, companyId, request);
	}

	@PutMapping("/fiscal-settings")
	FiscalSettingsResponse upsertSettings(
			@PathVariable String tenantId,
			@PathVariable String companyId,
			@Valid @RequestBody FiscalSettingsRequest body,
			HttpServletRequest request) {
		return service.upsertSettings(tenantId, companyId, body, request);
	}

	@GetMapping("/invoice-series")
	List<InvoiceSeriesResponse> listSeries(
			@PathVariable String tenantId,
			@PathVariable String companyId,
			HttpServletRequest request) {
		return service.listSeries(tenantId, companyId, request);
	}

	@PostMapping("/invoice-series")
	@ResponseStatus(HttpStatus.CREATED)
	InvoiceSeriesResponse createSeries(
			@PathVariable String tenantId,
			@PathVariable String companyId,
			@Valid @RequestBody InvoiceSeriesRequest body,
			HttpServletRequest request) {
		return service.createSeries(tenantId, companyId, body, request);
	}

	@PatchMapping("/invoice-series/{seriesId}")
	InvoiceSeriesResponse updateSeries(
			@PathVariable String tenantId,
			@PathVariable String companyId,
			@PathVariable String seriesId,
			@Valid @RequestBody InvoiceSeriesRequest body,
			HttpServletRequest request) {
		return service.updateSeries(tenantId, companyId, seriesId, body, request);
	}
}

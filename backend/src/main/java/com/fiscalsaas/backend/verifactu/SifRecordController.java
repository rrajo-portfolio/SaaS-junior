package com.fiscalsaas.backend.verifactu;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/tenants/{tenantId}/verifactu")
public class SifRecordController {

	private final SifRecordService service;

	SifRecordController(SifRecordService service) {
		this.service = service;
	}

	@GetMapping("/records")
	List<SifRecordResponse> records(@PathVariable String tenantId, HttpServletRequest request) {
		return service.listRecords(tenantId, request);
	}

	@GetMapping("/records/verify")
	SifHashChainVerificationResponse verify(@PathVariable String tenantId, HttpServletRequest request) {
		return service.verifyHashChain(tenantId, request);
	}

	@PostMapping("/records")
	@ResponseStatus(HttpStatus.CREATED)
	SifRecordResponse registerInvoice(
			@PathVariable String tenantId,
			@Valid @RequestBody CreateSifRecordRequest body,
			HttpServletRequest request) {
		return service.registerInvoice(tenantId, body, request);
	}

	@PatchMapping("/records/{recordId}/cancel")
	SifRecordResponse cancelRecord(
			@PathVariable String tenantId,
			@PathVariable String recordId,
			@Valid @RequestBody(required = false) CancelSifRecordRequest body,
			HttpServletRequest request) {
		return service.cancelRecord(tenantId, recordId, body, request);
	}

	@GetMapping("/records/{recordId}/events")
	List<SifEventResponse> recordEvents(
			@PathVariable String tenantId,
			@PathVariable String recordId,
			HttpServletRequest request) {
		return service.listRecordEvents(tenantId, recordId, request);
	}

	@GetMapping("/exports")
	List<SifExportBatchResponse> exports(@PathVariable String tenantId, HttpServletRequest request) {
		return service.listExports(tenantId, request);
	}

	@GetMapping("/exports/{exportId}")
	SifExportBatchResponse export(@PathVariable String tenantId, @PathVariable String exportId, HttpServletRequest request) {
		return service.getExport(tenantId, exportId, request);
	}

	@PostMapping("/exports")
	@ResponseStatus(HttpStatus.CREATED)
	SifExportBatchResponse createExport(@PathVariable String tenantId, HttpServletRequest request) {
		return service.createExport(tenantId, request);
	}
}

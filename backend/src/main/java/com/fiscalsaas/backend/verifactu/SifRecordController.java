package com.fiscalsaas.backend.verifactu;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
	private final VerifactuComplianceService complianceService;

	SifRecordController(SifRecordService service, VerifactuComplianceService complianceService) {
		this.service = service;
		this.complianceService = complianceService;
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

	@GetMapping("/records/{recordId}/qr")
	SifQrPayloadResponse qrPayload(
			@PathVariable String tenantId,
			@PathVariable String recordId,
			HttpServletRequest request) {
		return complianceService.qrPayload(tenantId, recordId, request);
	}

	@GetMapping(value = "/records/{recordId}/qr.svg", produces = "image/svg+xml")
	ResponseEntity<String> qrSvg(
			@PathVariable String tenantId,
			@PathVariable String recordId,
			HttpServletRequest request) {
		return ResponseEntity.ok()
				.cacheControl(CacheControl.noStore())
				.contentType(MediaType.valueOf("image/svg+xml"))
				.body(complianceService.qrSvg(tenantId, recordId, request));
	}

	@GetMapping("/records/{recordId}/transmissions")
	List<SifTransmissionAttemptResponse> transmissions(
			@PathVariable String tenantId,
			@PathVariable String recordId,
			HttpServletRequest request) {
		return complianceService.listTransmissions(tenantId, recordId, request);
	}

	@PostMapping("/records/{recordId}/transmissions")
	@ResponseStatus(HttpStatus.CREATED)
	SifTransmissionAttemptResponse transmit(
			@PathVariable String tenantId,
			@PathVariable String recordId,
			@Valid @RequestBody(required = false) CreateAeatTransmissionRequest body,
			HttpServletRequest request) {
		return complianceService.transmit(tenantId, recordId, body, request);
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

	@GetMapping("/system-declarations/drafts")
	List<SifSystemDeclarationResponse> systemDeclarationDrafts(@PathVariable String tenantId, HttpServletRequest request) {
		return complianceService.listSystemDeclarationDrafts(tenantId, request);
	}

	@PostMapping("/system-declarations/drafts")
	@ResponseStatus(HttpStatus.CREATED)
	SifSystemDeclarationResponse createSystemDeclarationDraft(@PathVariable String tenantId, HttpServletRequest request) {
		return complianceService.createSystemDeclarationDraft(tenantId, request);
	}
}

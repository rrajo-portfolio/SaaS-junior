package com.fiscalsaas.backend.fiscal;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenants/{tenantId}/companies/{companyId}/exports")
public class EvidenceExportController {

	private final EvidenceExportService service;

	EvidenceExportController(EvidenceExportService service) {
		this.service = service;
	}

	@GetMapping
	List<EvidenceExportResponse> listExports(
			@PathVariable String tenantId,
			@PathVariable String companyId,
			HttpServletRequest request) {
		return service.listExports(tenantId, companyId, request);
	}

	@PostMapping("/evidence-pack")
	@ResponseStatus(HttpStatus.CREATED)
	EvidenceExportResponse createExport(
			@PathVariable String tenantId,
			@PathVariable String companyId,
			HttpServletRequest request) {
		return service.createExport(tenantId, companyId, request);
	}

	@GetMapping("/{exportId}/download")
	ResponseEntity<byte[]> downloadExport(
			@PathVariable String tenantId,
			@PathVariable String companyId,
			@PathVariable String exportId,
			HttpServletRequest request) {
		EvidenceExportDownload download = service.downloadExport(tenantId, companyId, exportId, request);
		return ResponseEntity.ok()
				.cacheControl(CacheControl.noStore())
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.filename() + "\"")
				.header("X-Content-SHA256", download.sha256())
				.contentType(MediaType.valueOf("application/zip"))
				.body(download.bytes());
	}
}

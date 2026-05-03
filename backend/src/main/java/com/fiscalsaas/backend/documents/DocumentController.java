package com.fiscalsaas.backend.documents;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tenants/{tenantId}/documents")
public class DocumentController {

	private final DocumentService documentService;

	DocumentController(DocumentService documentService) {
		this.documentService = documentService;
	}

	@GetMapping
	List<DocumentResponse> documents(@PathVariable String tenantId, HttpServletRequest request) {
		return documentService.listDocuments(tenantId, request);
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ResponseEntity<DocumentResponse> upload(
			@PathVariable String tenantId,
			@RequestParam String companyId,
			@RequestParam String documentType,
			@RequestParam(required = false) String title,
			@RequestParam MultipartFile file,
			HttpServletRequest request) {
		return ResponseEntity.status(201).body(documentService.uploadDocument(tenantId, companyId, documentType, title, file, request));
	}

	@PostMapping(path = "/{documentId}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ResponseEntity<DocumentResponse> uploadVersion(
			@PathVariable String tenantId,
			@PathVariable String documentId,
			@RequestParam MultipartFile file,
			HttpServletRequest request) {
		return ResponseEntity.status(201).body(documentService.uploadVersion(tenantId, documentId, file, request));
	}

	@GetMapping("/{documentId}/download")
	ResponseEntity<Resource> download(@PathVariable String tenantId, @PathVariable String documentId, HttpServletRequest request) {
		DocumentDownload download = documentService.download(tenantId, documentId, request);
		return ResponseEntity.ok()
				.cacheControl(CacheControl.noStore())
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
						.filename(download.version().originalFilename())
						.build()
						.toString())
				.contentType(MediaType.parseMediaType(download.version().contentType()))
				.body(download.resource());
	}

	@GetMapping("/{documentId}/events")
	List<DocumentEventResponse> events(@PathVariable String tenantId, @PathVariable String documentId, HttpServletRequest request) {
		return documentService.events(tenantId, documentId, request);
	}
}

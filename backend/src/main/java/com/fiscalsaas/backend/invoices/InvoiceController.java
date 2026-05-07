package com.fiscalsaas.backend.invoices;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenants/{tenantId}/invoices")
public class InvoiceController {

	private final InvoiceService invoiceService;

	InvoiceController(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	@GetMapping
	List<InvoiceResponse> invoices(
			@PathVariable String tenantId,
			@RequestParam(required = false) String companyId,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String search,
			HttpServletRequest request) {
		return invoiceService.listInvoices(tenantId, companyId, status, search, request);
	}

	@GetMapping("/{invoiceId}")
	InvoiceResponse invoice(@PathVariable String tenantId, @PathVariable String invoiceId, HttpServletRequest request) {
		return invoiceService.getInvoice(tenantId, invoiceId, request);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	InvoiceResponse createInvoice(
			@PathVariable String tenantId,
			@Valid @RequestBody CreateInvoiceRequest body,
			HttpServletRequest request) {
		return invoiceService.createInvoice(tenantId, body, request);
	}

	@PatchMapping("/{invoiceId}")
	InvoiceResponse updateInvoice(
			@PathVariable String tenantId,
			@PathVariable String invoiceId,
			@Valid @RequestBody CreateInvoiceRequest body,
			HttpServletRequest request) {
		return invoiceService.updateInvoice(tenantId, invoiceId, body, request);
	}

	@PatchMapping("/{invoiceId}/status")
	InvoiceResponse updateStatus(
			@PathVariable String tenantId,
			@PathVariable String invoiceId,
			@Valid @RequestBody UpdateInvoiceStatusRequest body,
			HttpServletRequest request) {
		return invoiceService.updateStatus(tenantId, invoiceId, body, request);
	}

	@PostMapping("/{invoiceId}/issue")
	InvoiceResponse issueInvoice(
			@PathVariable String tenantId,
			@PathVariable String invoiceId,
			@Valid @RequestBody(required = false) IssueInvoiceRequest body,
			HttpServletRequest request) {
		return invoiceService.issueInvoice(tenantId, invoiceId, body, request);
	}

	@PostMapping("/{invoiceId}/cancel-local")
	InvoiceResponse cancelLocal(
			@PathVariable String tenantId,
			@PathVariable String invoiceId,
			@Valid @RequestBody(required = false) CancelInvoiceRequest body,
			HttpServletRequest request) {
		return invoiceService.cancelLocal(tenantId, invoiceId, body, request);
	}

	@PostMapping("/{invoiceId}/create-corrective")
	@ResponseStatus(HttpStatus.CREATED)
	InvoiceResponse createCorrective(
			@PathVariable String tenantId,
			@PathVariable String invoiceId,
			HttpServletRequest request) {
		return invoiceService.createCorrective(tenantId, invoiceId, request);
	}

	@GetMapping("/{invoiceId}/payments")
	List<InvoicePaymentResponse> listPayments(
			@PathVariable String tenantId,
			@PathVariable String invoiceId,
			HttpServletRequest request) {
		return invoiceService.listPayments(tenantId, invoiceId, request);
	}

	@PostMapping("/{invoiceId}/payments")
	@ResponseStatus(HttpStatus.CREATED)
	InvoicePaymentResponse addPayment(
			@PathVariable String tenantId,
			@PathVariable String invoiceId,
			@Valid @RequestBody CreatePaymentRequest body,
			HttpServletRequest request) {
		return invoiceService.addPayment(tenantId, invoiceId, body, request);
	}

	@GetMapping(value = "/{invoiceId}/pdf", produces = "application/pdf")
	ResponseEntity<byte[]> invoicePdf(
			@PathVariable String tenantId,
			@PathVariable String invoiceId,
			HttpServletRequest request) {
		InvoicePdfDownload download = invoiceService.downloadPdf(tenantId, invoiceId, request);
		return ResponseEntity.ok()
				.cacheControl(CacheControl.noStore())
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.filename() + "\"")
				.header("X-Content-SHA256", download.sha256())
				.contentType(MediaType.APPLICATION_PDF)
				.body(download.bytes());
	}
}

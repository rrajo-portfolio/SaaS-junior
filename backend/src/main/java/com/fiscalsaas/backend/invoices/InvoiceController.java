package com.fiscalsaas.backend.invoices;

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
@RequestMapping("/api/tenants/{tenantId}/invoices")
public class InvoiceController {

	private final InvoiceService invoiceService;

	InvoiceController(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	@GetMapping
	List<InvoiceResponse> invoices(@PathVariable String tenantId, HttpServletRequest request) {
		return invoiceService.listInvoices(tenantId, request);
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

	@PatchMapping("/{invoiceId}/status")
	InvoiceResponse updateStatus(
			@PathVariable String tenantId,
			@PathVariable String invoiceId,
			@Valid @RequestBody UpdateInvoiceStatusRequest body,
			HttpServletRequest request) {
		return invoiceService.updateStatus(tenantId, invoiceId, body, request);
	}
}

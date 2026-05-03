package com.fiscalsaas.backend.einvoice;

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
@RequestMapping("/api/tenants/{tenantId}/einvoices")
public class EInvoiceController {

	private final EInvoiceService service;

	EInvoiceController(EInvoiceService service) {
		this.service = service;
	}

	@GetMapping
	List<EInvoiceMessageResponse> messages(@PathVariable String tenantId, HttpServletRequest request) {
		return service.listMessages(tenantId, request);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	EInvoiceMessageResponse createMessage(
			@PathVariable String tenantId,
			@Valid @RequestBody CreateEInvoiceRequest body,
			HttpServletRequest request) {
		return service.createMessage(tenantId, body, request);
	}

	@GetMapping("/{messageId}")
	EInvoiceMessageResponse message(
			@PathVariable String tenantId,
			@PathVariable String messageId,
			HttpServletRequest request) {
		return service.getMessage(tenantId, messageId, request);
	}

	@GetMapping(value = "/{messageId}/payload", produces = MediaType.APPLICATION_XML_VALUE)
	ResponseEntity<String> payload(
			@PathVariable String tenantId,
			@PathVariable String messageId,
			HttpServletRequest request) {
		return ResponseEntity.ok()
				.cacheControl(CacheControl.noStore())
				.contentType(MediaType.APPLICATION_XML)
				.body(service.payload(tenantId, messageId, request));
	}

	@PatchMapping("/{messageId}/status")
	EInvoiceMessageResponse updateStatus(
			@PathVariable String tenantId,
			@PathVariable String messageId,
			@Valid @RequestBody UpdateEInvoiceStatusRequest body,
			HttpServletRequest request) {
		return service.updateStatus(tenantId, messageId, body, request);
	}

	@GetMapping("/{messageId}/events")
	List<EInvoiceEventResponse> events(
			@PathVariable String tenantId,
			@PathVariable String messageId,
			HttpServletRequest request) {
		return service.listEvents(tenantId, messageId, request);
	}

	@GetMapping("/{messageId}/payment-events")
	List<EInvoicePaymentEventResponse> paymentEvents(
			@PathVariable String tenantId,
			@PathVariable String messageId,
			HttpServletRequest request) {
		return service.listPaymentEvents(tenantId, messageId, request);
	}

	@PostMapping("/{messageId}/payment-events")
	@ResponseStatus(HttpStatus.CREATED)
	EInvoicePaymentEventResponse createPaymentEvent(
			@PathVariable String tenantId,
			@PathVariable String messageId,
			@Valid @RequestBody CreateEInvoicePaymentEventRequest body,
			HttpServletRequest request) {
		return service.createPaymentEvent(tenantId, messageId, body, request);
	}
}

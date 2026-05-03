package com.fiscalsaas.backend.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SifRecordControllerTests {

	private static final String TENANT_NORTE = "10000000-0000-0000-0000-000000000001";
	private static final String TENANT_COBALTO = "10000000-0000-0000-0000-000000000002";
	private static final String ISSUER_NORTE = "40000000-0000-0000-0000-000000000001";
	private static final String CUSTOMER_NORTE = "40000000-0000-0000-0000-000000000002";
	private static final String ANA = "ana.admin@fiscalsaas.local";
	private static final String MARIA_AUDITOR = "maria.auditor@fiscalsaas.local";

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void createsRegistrationCancellationEventsAndExportWithoutMutatingSourceRecord() throws Exception {
		String invoiceId = createIssuedInvoice("SIF-" + System.nanoTime());

		String registrationResponse = mockMvc.perform(post("/api/tenants/{tenantId}/verifactu/records", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "invoiceId": "%s"
								}
								""".formatted(invoiceId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.recordType").value("REGISTRATION"))
				.andExpect(jsonPath("$.recordHash").isNotEmpty())
				.andExpect(jsonPath("$.canonicalPayload").value(org.hamcrest.Matchers.containsString("\"invoiceStatus\":\"ISSUED\"")))
				.andReturn()
				.getResponse()
				.getContentAsString();
		JsonNode registration = objectMapper.readTree(registrationResponse);
		String registrationId = registration.get("id").asText();
		String registrationHash = registration.get("recordHash").asText();
		long registrationSequence = registration.get("sequenceNumber").asLong();

		String cancellationResponse = mockMvc.perform(patch("/api/tenants/{tenantId}/verifactu/records/{recordId}/cancel", TENANT_NORTE, registrationId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "reason": "Factura anulada en prueba"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.recordType").value("CANCELLATION"))
				.andExpect(jsonPath("$.sourceRecordId").value(registrationId))
				.andExpect(jsonPath("$.previousHash").value(registrationHash))
				.andReturn()
				.getResponse()
				.getContentAsString();
		JsonNode cancellation = objectMapper.readTree(cancellationResponse);
		String cancellationId = cancellation.get("id").asText();

		if (cancellation.get("sequenceNumber").asLong() != registrationSequence + 1) {
			throw new AssertionError("Cancellation record must be appended after registration.");
		}

		mockMvc.perform(get("/api/tenants/{tenantId}/verifactu/records/{recordId}/events", TENANT_NORTE, cancellationId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].eventType").value("RECORD_CANCELLED"));

		mockMvc.perform(get("/api/tenants/{tenantId}/verifactu/records/verify", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.valid").value(true))
				.andExpect(jsonPath("$.recordCount").isNumber());

		mockMvc.perform(post("/api/tenants/{tenantId}/verifactu/exports", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.recordCount").isNumber())
				.andExpect(jsonPath("$.exportSha256").isNotEmpty())
				.andExpect(jsonPath("$.payload").value(org.hamcrest.Matchers.containsString(registrationHash)));
	}

	@Test
	void rejectsDuplicateRegistrationDraftInvoiceAndSecondCancellation() throws Exception {
		String draftInvoiceId = createDraftInvoice("SIF-DRAFT-" + System.nanoTime());

		mockMvc.perform(post("/api/tenants/{tenantId}/verifactu/records", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "invoiceId": "%s"
								}
								""".formatted(draftInvoiceId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation_error"));

		String issuedInvoiceId = createIssuedInvoice("SIF-DUP-" + System.nanoTime());
		String registrationResponse = registerInvoice(issuedInvoiceId);
		String registrationId = objectMapper.readTree(registrationResponse).get("id").asText();

		mockMvc.perform(post("/api/tenants/{tenantId}/verifactu/records", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "invoiceId": "%s"
								}
								""".formatted(issuedInvoiceId)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("conflict"));

		cancelRecord(registrationId);
		mockMvc.perform(patch("/api/tenants/{tenantId}/verifactu/records/{recordId}/cancel", TENANT_NORTE, registrationId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("conflict"));
	}

	@Test
	void blocksCrossTenantReadsAndReadOnlyWrites() throws Exception {
		mockMvc.perform(get("/api/tenants/{tenantId}/verifactu/records", TENANT_COBALTO)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("tenant_access_denied"));

		mockMvc.perform(post("/api/tenants/{tenantId}/verifactu/records", TENANT_COBALTO)
						.header("X-User-Email", MARIA_AUDITOR)
						.header("X-Tenant-Id", TENANT_COBALTO)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "invoiceId": "00000000-0000-0000-0000-000000000000"
								}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("tenant_access_denied"));
	}

	private String createIssuedInvoice(String invoiceNumber) throws Exception {
		String invoiceId = createDraftInvoice(invoiceNumber);
		mockMvc.perform(patch("/api/tenants/{tenantId}/invoices/{invoiceId}/status", TENANT_NORTE, invoiceId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "status": "ISSUED"
								}
								"""))
				.andExpect(status().isOk());
		return invoiceId;
	}

	private String createDraftInvoice(String invoiceNumber) throws Exception {
		String response = mockMvc.perform(post("/api/tenants/{tenantId}/invoices", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(invoicePayload(invoiceNumber)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return objectMapper.readTree(response).get("id").asText();
	}

	private String registerInvoice(String invoiceId) throws Exception {
		return mockMvc.perform(post("/api/tenants/{tenantId}/verifactu/records", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "invoiceId": "%s"
								}
								""".formatted(invoiceId)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
	}

	private void cancelRecord(String recordId) throws Exception {
		mockMvc.perform(patch("/api/tenants/{tenantId}/verifactu/records/{recordId}/cancel", TENANT_NORTE, recordId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isOk());
	}

	private String invoicePayload(String invoiceNumber) {
		return """
				{
				  "issuerCompanyId": "%s",
				  "customerCompanyId": "%s",
				  "invoiceNumber": "%s",
				  "invoiceType": "ISSUED",
				  "issueDate": "2026-05-03",
				  "currency": "EUR",
				  "lines": [
				    {
				      "description": "Servicios fiscales SIF",
				      "quantity": 1,
				      "unitPrice": 100.00,
				      "taxRate": 21.00
				    }
				  ]
				}
				""".formatted(ISSUER_NORTE, CUSTOMER_NORTE, invoiceNumber);
	}
}

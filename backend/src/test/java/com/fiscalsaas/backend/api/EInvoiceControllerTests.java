package com.fiscalsaas.backend.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
class EInvoiceControllerTests {

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
	void createsUblMessageAndTracksAcceptanceAndPaymentEvents() throws Exception {
		String invoiceId = createIssuedInvoice("EI-UBL-" + System.nanoTime());

		String response = mockMvc.perform(post("/api/tenants/{tenantId}/einvoices", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "invoiceId": "%s",
								  "syntax": "UBL"
								}
								""".formatted(invoiceId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.syntax").value("UBL"))
				.andExpect(jsonPath("$.direction").value("OUTBOUND"))
				.andExpect(jsonPath("$.exchangeStatus").value("GENERATED"))
				.andExpect(jsonPath("$.commercialStatus").value("PENDING"))
				.andExpect(jsonPath("$.paymentStatus").value("UNPAID"))
				.andExpect(jsonPath("$.payloadSha256").value(org.hamcrest.Matchers.matchesPattern("[0-9a-f]{64}")))
				.andReturn()
				.getResponse()
				.getContentAsString();
		JsonNode message = objectMapper.readTree(response);
		String messageId = message.get("id").asText();

		mockMvc.perform(get("/api/tenants/{tenantId}/einvoices/{messageId}/payload", TENANT_NORTE, messageId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("<ubl:Invoice")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("B2B_PREPARATION")));

		mockMvc.perform(patch("/api/tenants/{tenantId}/einvoices/{messageId}/status", TENANT_NORTE, messageId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "exchangeStatus": "sent",
								  "commercialStatus": "accepted",
								  "reason": "Aceptada por el cliente"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.exchangeStatus").value("SENT"))
				.andExpect(jsonPath("$.commercialStatus").value("ACCEPTED"))
				.andExpect(jsonPath("$.statusReason").value("Aceptada por el cliente"));

		mockMvc.perform(post("/api/tenants/{tenantId}/einvoices/{messageId}/payment-events", TENANT_NORTE, messageId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "amount": 121.00,
								  "paymentStatus": "paid",
								  "reference": "TR-001",
								  "notes": "Cobro completo"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.paymentStatus").value("PAID"))
				.andExpect(jsonPath("$.amount").value(121.00));

		mockMvc.perform(get("/api/tenants/{tenantId}/einvoices/{messageId}", TENANT_NORTE, messageId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paymentStatus").value("PAID"));

		mockMvc.perform(get("/api/tenants/{tenantId}/einvoices/{messageId}/events", TENANT_NORTE, messageId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].eventType").value("PAYMENT_STATUS_CHANGED"));

		mockMvc.perform(get("/api/tenants/{tenantId}/einvoices/{messageId}/payment-events", TENANT_NORTE, messageId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].paymentReference").value("TR-001"));
	}

	@Test
	void supportsFacturaeDraftAndRejectsDuplicateOrDraftInvoice() throws Exception {
		String facturaeInvoiceId = createIssuedInvoice("EI-FE-" + System.nanoTime());

		String facturaeResponse = mockMvc.perform(post("/api/tenants/{tenantId}/einvoices", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "invoiceId": "%s",
								  "syntax": "facturae"
								}
								""".formatted(facturaeInvoiceId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.syntax").value("FACTURAE"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		String messageId = objectMapper.readTree(facturaeResponse).get("id").asText();

		mockMvc.perform(get("/api/tenants/{tenantId}/einvoices/{messageId}/payload", TENANT_NORTE, messageId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("<Facturae>")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("<SchemaVersion>3.2.2</SchemaVersion>")));

		mockMvc.perform(post("/api/tenants/{tenantId}/einvoices", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "invoiceId": "%s"
								}
								""".formatted(facturaeInvoiceId)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("conflict"));

		String draftInvoiceId = createDraftInvoice("EI-DRAFT-" + System.nanoTime());
		mockMvc.perform(post("/api/tenants/{tenantId}/einvoices", TENANT_NORTE)
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
	}

	@Test
	void blocksCrossTenantReadsAndReadOnlyWrites() throws Exception {
		mockMvc.perform(get("/api/tenants/{tenantId}/einvoices", TENANT_COBALTO)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("tenant_access_denied"));

		mockMvc.perform(post("/api/tenants/{tenantId}/einvoices", TENANT_COBALTO)
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
				      "description": "Servicios e-invoice",
				      "quantity": 1,
				      "unitPrice": 100.00,
				      "taxRate": 21.00
				    }
				  ]
				}
				""".formatted(ISSUER_NORTE, CUSTOMER_NORTE, invoiceNumber);
	}
}

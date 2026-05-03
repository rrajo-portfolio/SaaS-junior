package com.fiscalsaas.backend.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class InvoiceControllerTests {

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
	void createsListsAndIssuesInvoiceWithCalculatedTotals() throws Exception {
		String invoiceResponse = mockMvc.perform(post("/api/tenants/{tenantId}/invoices", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(invoicePayload("F2026-0001")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.invoiceNumber").value("F2026-0001"))
				.andExpect(jsonPath("$.status").value("DRAFT"))
				.andExpect(jsonPath("$.taxableBase").value(250.00))
				.andExpect(jsonPath("$.taxTotal").value(47.00))
				.andExpect(jsonPath("$.total").value(297.00))
				.andExpect(jsonPath("$.lines[0].lineBase").value(200.00))
				.andExpect(jsonPath("$.lines[1].taxAmount").value(5.00))
				.andExpect(jsonPath("$.taxes[0].taxRate").value(10.00))
				.andExpect(jsonPath("$.taxes[1].taxRate").value(21.00))
				.andReturn()
				.getResponse()
				.getContentAsString();
		String invoiceId = objectMapper.readTree(invoiceResponse).get("id").asText();

		mockMvc.perform(get("/api/tenants/{tenantId}/invoices", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].tenantId").value(TENANT_NORTE));

		mockMvc.perform(get("/api/tenants/{tenantId}/invoices/{invoiceId}", TENANT_NORTE, invoiceId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.invoiceNumber").value("F2026-0001"));

		mockMvc.perform(patch("/api/tenants/{tenantId}/invoices/{invoiceId}/status", TENANT_NORTE, invoiceId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "status": "ISSUED"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ISSUED"));
	}

	@Test
	void rejectsDuplicateInvoiceNumbersInsideTenant() throws Exception {
		mockMvc.perform(post("/api/tenants/{tenantId}/invoices", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(invoicePayload("F2026-0002")))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/tenants/{tenantId}/invoices", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(invoicePayload("F2026-0002")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("conflict"));
	}

	@Test
	void rejectsRectifyingInvoiceWithoutOriginalInvoice() throws Exception {
		String payload = invoicePayload("R2026-0001").replace("\"invoiceType\": \"ISSUED\"", "\"invoiceType\": \"RECTIFYING\"");

		mockMvc.perform(post("/api/tenants/{tenantId}/invoices", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation_error"));
	}

	@Test
	void blocksCrossTenantInvoiceAccessAndReadOnlyWrites() throws Exception {
		mockMvc.perform(get("/api/tenants/{tenantId}/invoices", TENANT_COBALTO)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("tenant_access_denied"));

		mockMvc.perform(post("/api/tenants/{tenantId}/invoices", TENANT_COBALTO)
						.header("X-User-Email", MARIA_AUDITOR)
						.header("X-Tenant-Id", TENANT_COBALTO)
						.contentType(MediaType.APPLICATION_JSON)
						.content(invoicePayload("F2026-0003")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("tenant_access_denied"));
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
				      "description": "Servicios fiscales",
				      "quantity": 2,
				      "unitPrice": 100.00,
				      "taxRate": 21.00
				    },
				    {
				      "description": "Suplido documental",
				      "quantity": 1,
				      "unitPrice": 50.00,
				      "taxRate": 10.00
				    }
				  ]
				}
				""".formatted(ISSUER_NORTE, CUSTOMER_NORTE, invoiceNumber);
	}
}

package com.fiscalsaas.backend.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
	void editsDraftInvoicesAndBlocksEditionAfterIssue() throws Exception {
		String invoiceResponse = mockMvc.perform(post("/api/tenants/{tenantId}/invoices", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(invoicePayload("F2026-0101")))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		String invoiceId = objectMapper.readTree(invoiceResponse).get("id").asText();

		String updatedPayload = """
				{
				  "issuerCompanyId": "%s",
				  "customerCompanyId": "%s",
				  "invoiceNumber": "F2026-0101-EDIT",
				  "invoiceType": "ISSUED",
				  "issueDate": "2026-05-04",
				  "currency": "EUR",
				  "lines": [
				    {
				      "description": "Servicio demo editado",
				      "quantity": 1,
				      "unitPrice": 100.00,
				      "taxRate": 21.00
				    }
				  ]
				}
				""".formatted(ISSUER_NORTE, CUSTOMER_NORTE);

		mockMvc.perform(patch("/api/tenants/{tenantId}/invoices/{invoiceId}", TENANT_NORTE, invoiceId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updatedPayload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.invoiceNumber").value("F2026-0101-EDIT"))
				.andExpect(jsonPath("$.total").value(121.00))
				.andExpect(jsonPath("$.lines[0].description").value("Servicio demo editado"));

		mockMvc.perform(get("/api/tenants/{tenantId}/invoices", TENANT_NORTE)
						.param("companyId", CUSTOMER_NORTE)
						.param("status", "DRAFT")
						.param("search", "0101-EDIT")
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].invoiceNumber").value("F2026-0101-EDIT"));

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

		mockMvc.perform(patch("/api/tenants/{tenantId}/invoices/{invoiceId}", TENANT_NORTE, invoiceId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updatedPayload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation_error"));
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

	@Test
	void supportsFunctionalSaasFiscalFlowWithCustomerPaymentPdfAuditAndExport() throws Exception {
		String customerResponse = mockMvc.perform(post("/api/tenants/{tenantId}/companies/{companyId}/customers", TENANT_NORTE, ISSUER_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "customerType": "COMPANY",
								  "name": "Cliente Funcional SL",
								  "nif": "B66554433",
								  "email": "facturas@cliente.local",
								  "addressLine1": "Calle Cliente 7",
								  "city": "Madrid",
								  "province": "Madrid",
								  "postalCode": "28003",
								  "country": "ES"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Cliente Funcional SL"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		String customerId = objectMapper.readTree(customerResponse).get("id").asText();

		String invoiceNumber = "FLOW-" + System.nanoTime();
		String invoiceResponse = mockMvc.perform(post("/api/tenants/{tenantId}/invoices", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "issuerCompanyId": "%s",
								  "customerCompanyId": "%s",
								  "customerId": "%s",
								  "invoiceNumber": "%s",
								  "invoiceType": "STANDARD",
								  "issueDate": "2026-05-07",
								  "dueDate": "2026-06-06",
								  "currency": "EUR",
								  "lines": [
								    {
								      "description": "Servicio con descuento y retencion",
								      "quantity": 1,
								      "unitPrice": 100.00,
								      "taxRate": 21.00,
								      "discountPercent": 10.00,
								      "withholdingPercent": 15.00
								    }
								  ]
								}
								""".formatted(ISSUER_NORTE, CUSTOMER_NORTE, customerId, invoiceNumber)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.taxableBase").value(90.00))
				.andExpect(jsonPath("$.taxTotal").value(18.90))
				.andExpect(jsonPath("$.withholdingTotal").value(13.50))
				.andExpect(jsonPath("$.total").value(95.40))
				.andReturn()
				.getResponse()
				.getContentAsString();
		String invoiceId = objectMapper.readTree(invoiceResponse).get("id").asText();

		mockMvc.perform(post("/api/tenants/{tenantId}/invoices/{invoiceId}/issue", TENANT_NORTE, invoiceId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "issueRequestId": "flow-issue-%s"
								}
								""".formatted(invoiceId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ISSUED"))
				.andExpect(jsonPath("$.fiscalNumber").value(org.hamcrest.Matchers.startsWith("F-2026-")))
				.andExpect(jsonPath("$.customerSnapshot").value(org.hamcrest.Matchers.containsString("Cliente Funcional SL")))
				.andExpect(jsonPath("$.outstandingAmount").value(95.40));

		mockMvc.perform(get("/api/tenants/{tenantId}/invoices/{invoiceId}/pdf", TENANT_NORTE, invoiceId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
				.andExpect(header().string("X-Content-SHA256", org.hamcrest.Matchers.matchesPattern("[0-9a-f]{64}")));

		mockMvc.perform(post("/api/tenants/{tenantId}/invoices/{invoiceId}/payments", TENANT_NORTE, invoiceId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "amount": 95.40,
								  "paymentDate": "2026-05-08",
								  "method": "BANK_TRANSFER",
								  "reference": "TRF-001"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.amount").value(95.40));

		mockMvc.perform(get("/api/tenants/{tenantId}/invoices/{invoiceId}", TENANT_NORTE, invoiceId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paymentStatus").value("PAID"))
				.andExpect(jsonPath("$.outstandingAmount").value(0.00));

		mockMvc.perform(post("/api/tenants/{tenantId}/invoices/{invoiceId}/create-corrective", TENANT_NORTE, invoiceId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("DRAFT"))
				.andExpect(jsonPath("$.invoiceType").value("CORRECTIVE"))
				.andExpect(jsonPath("$.rectifiesInvoiceId").value(invoiceId));

		mockMvc.perform(get("/api/tenants/{tenantId}/companies/{companyId}/audit-events", TENANT_NORTE, ISSUER_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].eventHash").value(org.hamcrest.Matchers.matchesPattern("[0-9a-f]{64}")));

		String exportResponse = mockMvc.perform(post("/api/tenants/{tenantId}/companies/{companyId}/exports/evidence-pack", TENANT_NORTE, ISSUER_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("COMPLETED"))
				.andExpect(jsonPath("$.sha256").value(org.hamcrest.Matchers.matchesPattern("[0-9a-f]{64}")))
				.andReturn()
				.getResponse()
				.getContentAsString();
		String exportId = objectMapper.readTree(exportResponse).get("id").asText();

		mockMvc.perform(get("/api/tenants/{tenantId}/companies/{companyId}/exports/{exportId}/download", TENANT_NORTE, ISSUER_NORTE, exportId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/zip")))
				.andExpect(header().string("X-Content-SHA256", org.hamcrest.Matchers.matchesPattern("[0-9a-f]{64}")));
	}

	@Test
	void blocksIssuingWhenIssuerHasNoFiscalSettings() throws Exception {
		String companyResponse = mockMvc.perform(post("/api/tenants/{tenantId}/companies", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "legalName": "Sin Fiscal Settings SL",
								  "taxId": "B66778899",
								  "countryCode": "ES",
								  "relationshipType": "OWNER"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		String companyId = objectMapper.readTree(companyResponse).get("id").asText();
		String invoiceResponse = mockMvc.perform(post("/api/tenants/{tenantId}/invoices", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(invoicePayload("NO-SETTINGS-" + System.nanoTime()).replace(ISSUER_NORTE, companyId)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		String invoiceId = objectMapper.readTree(invoiceResponse).get("id").asText();

		mockMvc.perform(post("/api/tenants/{tenantId}/invoices/{invoiceId}/issue", TENANT_NORTE, invoiceId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation_error"));
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

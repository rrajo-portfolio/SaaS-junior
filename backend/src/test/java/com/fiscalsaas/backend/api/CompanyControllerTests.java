package com.fiscalsaas.backend.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class CompanyControllerTests {

	private static final String TENANT_NORTE = "10000000-0000-0000-0000-000000000001";
	private static final String TENANT_COBALTO = "10000000-0000-0000-0000-000000000002";
	private static final String ANA = "ana.admin@fiscalsaas.local";
	private static final String MARIA_AUDITOR = "maria.auditor@fiscalsaas.local";

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void listsTenantCompaniesAndRelationships() throws Exception {
		mockMvc.perform(get("/api/tenants/{tenantId}/companies", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].tenantId").value(TENANT_NORTE));

		mockMvc.perform(get("/api/tenants/{tenantId}/companies", TENANT_NORTE)
						.param("search", "Alba")
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].legalName").value("Alba Retail Group SL"));

		mockMvc.perform(get("/api/tenants/{tenantId}/companies", TENANT_NORTE)
						.param("search", "B87654321")
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].taxId").value("B87654321"));

		mockMvc.perform(get("/api/tenants/{tenantId}/business-relationships", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].relationshipKind").value("CLIENT_MANAGEMENT"));
	}

	@Test
	void createsAndUpdatesCompanyWithBasicTaxValidation() throws Exception {
		String createPayload = """
				{
				  "legalName": "Beta Servicios SL",
				  "taxId": "B11223344",
				  "countryCode": "ES",
				  "relationshipType": "CLIENT"
				}
				""";

		String location = mockMvc.perform(post("/api/tenants/{tenantId}/companies", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(createPayload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.legalName").value("Beta Servicios SL"))
				.andExpect(jsonPath("$.taxId").value("B11223344"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		String companyId = objectMapper.readTree(location).get("id").asText();
		String updatePayload = """
				{
				  "legalName": "Beta Servicios Profesionales SL",
				  "relationshipType": "SUPPLIER"
				}
				""";

		mockMvc.perform(patch("/api/tenants/{tenantId}/companies/{companyId}", TENANT_NORTE, companyId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updatePayload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.legalName").value("Beta Servicios Profesionales SL"))
				.andExpect(jsonPath("$.relationshipType").value("SUPPLIER"));
	}

	@Test
	void rejectsInvalidAndDuplicateTaxIds() throws Exception {
		String invalidPayload = """
				{
				  "legalName": "Invalid Fiscal SL",
				  "taxId": "123",
				  "countryCode": "ES",
				  "relationshipType": "CLIENT"
				}
				""";

		mockMvc.perform(post("/api/tenants/{tenantId}/companies", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidPayload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation_error"));

		String duplicatePayload = """
				{
				  "legalName": "Duplicate SL",
				  "taxId": "B12345678",
				  "countryCode": "ES",
				  "relationshipType": "CLIENT"
				}
				""";

		mockMvc.perform(post("/api/tenants/{tenantId}/companies", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(duplicatePayload))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("conflict"));
	}

	@Test
	void blocksWriteAccessForReadOnlyFiscalRoles() throws Exception {
		String payload = """
				{
				  "legalName": "Audited Supplier SL",
				  "taxId": "B55667788",
				  "countryCode": "ES",
				  "relationshipType": "SUPPLIER"
				}
				""";

		mockMvc.perform(post("/api/tenants/{tenantId}/companies", TENANT_COBALTO)
						.header("X-User-Email", MARIA_AUDITOR)
						.header("X-Tenant-Id", TENANT_COBALTO)
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("tenant_access_denied"));
	}

	@Test
	void createsAndDeactivatesBusinessRelationshipInsideTenantOnly() throws Exception {
		String companyPayload = """
				{
				  "legalName": "Gamma Retail SL",
				  "taxId": "B99887766",
				  "countryCode": "ES",
				  "relationshipType": "CLIENT"
				}
				""";
		String companyResponse = mockMvc.perform(post("/api/tenants/{tenantId}/companies", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(companyPayload))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		String targetCompanyId = objectMapper.readTree(companyResponse).get("id").asText();

		String relationshipPayload = """
				{
				  "sourceCompanyId": "40000000-0000-0000-0000-000000000001",
				  "targetCompanyId": "%s",
				  "relationshipKind": "DOCUMENT_EXCHANGE",
				  "notes": "Portal documental dedicado"
				}
				""".formatted(targetCompanyId);

		String relationshipResponse = mockMvc.perform(post("/api/tenants/{tenantId}/business-relationships", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(relationshipPayload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.relationshipKind").value("DOCUMENT_EXCHANGE"))
				.andExpect(jsonPath("$.targetCompany.legalName").value("Gamma Retail SL"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		String relationshipId = objectMapper.readTree(relationshipResponse).get("id").asText();

		mockMvc.perform(delete("/api/tenants/{tenantId}/business-relationships/{relationshipId}", TENANT_NORTE, relationshipId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isNoContent());
	}
}

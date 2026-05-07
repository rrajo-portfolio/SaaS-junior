package com.fiscalsaas.backend.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentControllerTests {

	private static final String TENANT_NORTE = "10000000-0000-0000-0000-000000000001";
	private static final String TENANT_COBALTO = "10000000-0000-0000-0000-000000000002";
	private static final String COMPANY_NORTE = "40000000-0000-0000-0000-000000000001";
	private static final String ANA = "ana.admin@fiscalsaas.local";
	private static final String MARIA_AUDITOR = "maria.auditor@fiscalsaas.local";

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void uploadsListsDownloadsAndAuditsTenantDocument() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"factura.txt",
				"text/plain",
				"factura demo sin datos reales".getBytes());

		String response = mockMvc.perform(multipart("/api/tenants/{tenantId}/documents", TENANT_NORTE)
						.file(file)
						.param("companyId", COMPANY_NORTE)
						.param("documentType", "INVOICE_RECEIVED")
						.param("title", "Factura recibida demo")
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Factura recibida demo"))
				.andExpect(jsonPath("$.currentVersion").value(1))
				.andExpect(jsonPath("$.latestSha256").isNotEmpty())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String documentId = objectMapper.readTree(response).get("id").asText();

		mockMvc.perform(get("/api/tenants/{tenantId}/documents", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].tenantId").value(TENANT_NORTE));

		mockMvc.perform(get("/api/tenants/{tenantId}/documents", TENANT_NORTE)
						.param("companyId", COMPANY_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].company.id").value(COMPANY_NORTE));

		mockMvc.perform(get("/api/tenants/{tenantId}/documents/{documentId}/download", TENANT_NORTE, documentId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(header().string("Cache-Control", "no-store"))
				.andExpect(content().string("factura demo sin datos reales"));

		mockMvc.perform(get("/api/tenants/{tenantId}/documents/{documentId}/events", TENANT_NORTE, documentId)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].eventType").value("DOCUMENT_UPLOADED"));
	}

	@Test
	void rejectsDocumentUploadForAuditorRole() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "audit.txt", MediaType.TEXT_PLAIN_VALUE, "no write".getBytes());

		mockMvc.perform(multipart("/api/tenants/{tenantId}/documents", TENANT_COBALTO)
						.file(file)
						.param("companyId", "40000000-0000-0000-0000-000000000003")
						.param("documentType", "EVIDENCE")
						.header("X-User-Email", MARIA_AUDITOR)
						.header("X-Tenant-Id", TENANT_COBALTO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("tenant_access_denied"));
	}

	@Test
	void blocksCrossTenantDocumentList() throws Exception {
		mockMvc.perform(get("/api/tenants/{tenantId}/documents", TENANT_COBALTO)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("tenant_access_denied"));
	}
}

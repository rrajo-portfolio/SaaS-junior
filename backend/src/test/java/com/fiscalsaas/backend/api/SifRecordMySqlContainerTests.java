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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SifRecordMySqlContainerTests {

	private static final String TENANT_NORTE = "10000000-0000-0000-0000-000000000001";
	private static final String ISSUER_NORTE = "40000000-0000-0000-0000-000000000001";
	private static final String CUSTOMER_NORTE = "40000000-0000-0000-0000-000000000002";
	private static final String ANA = "ana.admin@fiscalsaas.local";

	@Container
	static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

	@DynamicPropertySource
	static void mysqlProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysql::getJdbcUrl);
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
		registry.add("app.documents.storage-path", () -> "target/testcontainer-document-storage");
	}

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void persistsSifHashChainOnMySql() throws Exception {
		String invoiceResponse = mockMvc.perform(post("/api/tenants/{tenantId}/invoices", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(invoicePayload("TC-SIF-" + System.nanoTime())))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		String invoiceId = objectMapper.readTree(invoiceResponse).get("id").asText();

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

		mockMvc.perform(post("/api/tenants/{tenantId}/verifactu/records", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "invoiceId": "%s"
								}
								""".formatted(invoiceId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.recordHash").isNotEmpty());

		mockMvc.perform(get("/api/tenants/{tenantId}/verifactu/records/verify", TENANT_NORTE)
						.header("X-User-Email", ANA)
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.valid").value(true))
				.andExpect(jsonPath("$.recordCount").value(1));
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
				      "description": "Testcontainers SIF",
				      "quantity": 1,
				      "unitPrice": 100.00,
				      "taxRate": 21.00
				    }
				  ]
				}
				""".formatted(ISSUER_NORTE, CUSTOMER_NORTE, invoiceNumber);
	}
}

package com.fiscalsaas.backend.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SaasAdminControllerTests {

	private static final String ANA = "ana.admin@fiscalsaas.local";
	private static final String LEO = "leo.accountant@fiscalsaas.local";

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void listsSubscriptionPlansForAuthenticatedUsers() throws Exception {
		mockMvc.perform(get("/api/platform/plans")
						.header("X-User-Email", LEO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("starter"))
				.andExpect(jsonPath("$[1].code").value("professional"))
				.andExpect(jsonPath("$[2].code").value("business"));
	}

	@Test
	void createsTenantWithInitialPlanAndAdminMembership() throws Exception {
		Map<String, String> body = Map.of(
				"slug", "verde-fiscal",
				"displayName", "Verde Fiscal",
				"planCode", "professional",
				"adminEmail", "owner@verde-fiscal.local",
				"adminDisplayName", "Verde Owner");

		mockMvc.perform(post("/api/platform/tenants")
						.header("X-User-Email", ANA)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.slug").value("verde-fiscal"))
				.andExpect(jsonPath("$.planCode").value("professional"))
				.andExpect(jsonPath("$.subscriptionStatus").value("trialing"));
	}

	@Test
	void blocksTenantCreationForNonPlatformUsers() throws Exception {
		Map<String, String> body = Map.of(
				"slug", "blocked-fiscal",
				"displayName", "Blocked Fiscal",
				"planCode", "starter",
				"adminEmail", "owner@blocked-fiscal.local",
				"adminDisplayName", "Blocked Owner");

		mockMvc.perform(post("/api/platform/tenants")
						.header("X-User-Email", LEO)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("tenant_access_denied"));
	}

	@Test
	void changesTenantPlanAndWritesLifecycleEvent() throws Exception {
		String tenantId = createTenant("azul-fiscal", "owner@azul-fiscal.local");

		mockMvc.perform(patch("/api/platform/tenants/{tenantId}/plan", tenantId)
						.header("X-User-Email", ANA)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"planCode", "business",
								"notes", "Upgrade after review."))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.planCode").value("business"));

		mockMvc.perform(get("/api/platform/tenants/{tenantId}/events", tenantId)
						.header("X-User-Email", ANA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].eventType").value("tenant_plan_changed"))
				.andExpect(jsonPath("$[0].fromPlanCode").value("professional"))
				.andExpect(jsonPath("$[0].toPlanCode").value("business"));
	}

	@Test
	void suspendedTenantsCannotUseTenantScopedEndpoints() throws Exception {
		String tenantId = createTenant("rojo-fiscal", "owner@rojo-fiscal.local");

		mockMvc.perform(patch("/api/platform/tenants/{tenantId}/status", tenantId)
						.header("X-User-Email", ANA)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"status", "suspended",
								"notes", "Payment failure."))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUSPENDED"));

		mockMvc.perform(get("/api/tenants/{tenantId}/companies", tenantId)
						.header("X-User-Email", "owner@rojo-fiscal.local")
						.header("X-Tenant-Id", tenantId))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Tenant is not active."));
	}

	private String createTenant(String slug, String adminEmail) throws Exception {
		Map<String, String> body = Map.of(
				"slug", slug,
				"displayName", slug,
				"planCode", "professional",
				"adminEmail", adminEmail,
				"adminDisplayName", "Tenant Owner");

		MvcResult result = mockMvc.perform(post("/api/platform/tenants")
						.header("X-User-Email", ANA)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isCreated())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
	}
}

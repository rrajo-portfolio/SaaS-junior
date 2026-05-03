package com.fiscalsaas.backend.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IdentityControllerTests {

	private static final String TENANT_NORTE = "10000000-0000-0000-0000-000000000001";
	private static final String TENANT_COBALTO = "10000000-0000-0000-0000-000000000002";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void returnsCurrentUserMemberships() throws Exception {
		mockMvc.perform(get("/api/me")
						.header("X-User-Email", "ana.admin@fiscalsaas.local"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.user.email").value("ana.admin@fiscalsaas.local"))
				.andExpect(jsonPath("$.memberships.length()").value(2))
				.andExpect(jsonPath("$.memberships[0].role").value("platform_admin"));
	}

	@Test
	void allowsTenantMemberToReadCompanies() throws Exception {
		mockMvc.perform(get("/api/tenants/{tenantId}/companies", TENANT_NORTE)
						.header("X-User-Email", "leo.accountant@fiscalsaas.local")
						.header("X-Tenant-Id", TENANT_NORTE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].tenantId").value(TENANT_NORTE));
	}

	@Test
	void blocksCrossTenantCompanyAccess() throws Exception {
		mockMvc.perform(get("/api/tenants/{tenantId}/companies", TENANT_COBALTO)
						.header("X-User-Email", "leo.accountant@fiscalsaas.local")
						.header("X-Tenant-Id", TENANT_COBALTO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("tenant_access_denied"));
	}

	@Test
	void requiresAuthenticatedUserForTenantList() throws Exception {
		mockMvc.perform(get("/api/tenants"))
				.andExpect(status().isUnauthorized());
	}
}

package com.fiscalsaas.backend.api;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void healthEndpointIsPublic() throws Exception {
		mockMvc.perform(get("/api/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", equalTo("ok")))
				.andExpect(jsonPath("$.service", equalTo("fiscal-saas-backend")));
	}

	@Test
	void nonHealthEndpointsRequireAuthentication() throws Exception {
		mockMvc.perform(get("/api/private"))
				.andExpect(status().isUnauthorized());
	}
}

package com.fiscalsaas.backend.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiscalsaas.backend.security.CurrentUser;
import com.fiscalsaas.backend.security.JwtCurrentUserAuthenticationConverter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class OidcAuthenticationTests {

	@Autowired
	private JwtCurrentUserAuthenticationConverter converter;

	@Test
	void mapsOidcEmailClaimToApplicationUserAndRoles() {
		Jwt jwt = Jwt.withTokenValue("test-token")
				.header("alg", "none")
				.claim("email", "ana.admin@fiscalsaas.local")
				.subject("keycloak-user-id")
				.issuer("http://localhost:18081/realms/fiscal-saas")
				.build();

		var authentication = converter.convert(jwt);

		assertThat(authentication).isNotNull();
		assertThat(authentication.getPrincipal()).isInstanceOf(CurrentUser.class);
		CurrentUser user = (CurrentUser) authentication.getPrincipal();
		assertThat(user.email()).isEqualTo("ana.admin@fiscalsaas.local");
		assertThat(user.roles()).extracting(Enum::name).contains("PLATFORM_ADMIN");
	}
}

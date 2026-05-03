package com.fiscalsaas.backend.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtCurrentUserAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private final CurrentUserAuthenticationFactory authenticationFactory;

	JwtCurrentUserAuthenticationConverter(CurrentUserAuthenticationFactory authenticationFactory) {
		this.authenticationFactory = authenticationFactory;
	}

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		String email = jwt.getClaimAsString("email");
		if (email == null || email.isBlank()) {
			email = jwt.getClaimAsString("preferred_username");
		}
		return authenticationFactory.fromEmail(email, jwt.getTokenValue());
	}
}

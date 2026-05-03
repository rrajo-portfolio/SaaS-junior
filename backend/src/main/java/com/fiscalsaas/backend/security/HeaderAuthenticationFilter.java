package com.fiscalsaas.backend.security;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

	public static final String USER_EMAIL_HEADER = "X-User-Email";
	public static final String TENANT_ID_HEADER = "X-Tenant-Id";

	private final CurrentUserAuthenticationFactory authenticationFactory;

	HeaderAuthenticationFilter(CurrentUserAuthenticationFactory authenticationFactory) {
		this.authenticationFactory = authenticationFactory;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String email = request.getHeader(USER_EMAIL_HEADER);
		if (email != null && !email.isBlank()) {
			SecurityContextHolder.getContext().setAuthentication(authenticationFactory.fromEmail(email, "N/A"));
		}

		filterChain.doFilter(request, response);
	}
}

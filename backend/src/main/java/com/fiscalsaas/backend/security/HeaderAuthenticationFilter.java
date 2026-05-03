package com.fiscalsaas.backend.security;

import java.io.IOException;
import java.util.Locale;

import com.fiscalsaas.backend.identity.AppUserRepository;
import com.fiscalsaas.backend.identity.MembershipRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

	public static final String USER_EMAIL_HEADER = "X-User-Email";
	public static final String TENANT_ID_HEADER = "X-Tenant-Id";

	private final AppUserRepository users;
	private final MembershipRepository memberships;

	HeaderAuthenticationFilter(AppUserRepository users, MembershipRepository memberships) {
		this.users = users;
		this.memberships = memberships;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String email = request.getHeader(USER_EMAIL_HEADER);
		if (email != null && !email.isBlank()) {
			users.findByEmailIgnoreCaseAndStatus(email.trim().toLowerCase(Locale.ROOT), "ACTIVE")
					.ifPresent(user -> {
						var roles = memberships.findByUserIdAndStatusOrderByTenantDisplayNameAsc(user.id(), "ACTIVE")
								.stream()
								.map(membership -> membership.fiscalRole())
								.distinct()
								.toList();
						var authorities = roles.stream()
								.map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
								.toList();
						var principal = new CurrentUser(user.id(), user.email(), user.displayName(), roles);
						var authentication = new UsernamePasswordAuthenticationToken(principal, "N/A", authorities);
						SecurityContextHolder.getContext().setAuthentication(authentication);
					});
		}

		filterChain.doFilter(request, response);
	}
}

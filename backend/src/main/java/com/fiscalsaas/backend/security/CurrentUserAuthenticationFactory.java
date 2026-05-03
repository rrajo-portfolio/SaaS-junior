package com.fiscalsaas.backend.security;

import java.util.List;
import java.util.Locale;

import com.fiscalsaas.backend.identity.AppUserRepository;
import com.fiscalsaas.backend.identity.MembershipRepository;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserAuthenticationFactory {

	private final AppUserRepository users;
	private final MembershipRepository memberships;

	CurrentUserAuthenticationFactory(AppUserRepository users, MembershipRepository memberships) {
		this.users = users;
		this.memberships = memberships;
	}

	public UsernamePasswordAuthenticationToken fromEmail(String rawEmail, Object credentials) {
		if (rawEmail == null || rawEmail.isBlank()) {
			throw new UnknownApplicationUserException("Authenticated token does not contain an email claim.");
		}
		String email = rawEmail.trim().toLowerCase(Locale.ROOT);
		return users.findByEmailIgnoreCaseAndStatus(email, "ACTIVE")
				.map(user -> {
					List<com.fiscalsaas.backend.identity.FiscalRole> roles = memberships
							.findByUserIdAndStatusOrderByTenantDisplayNameAsc(user.id(), "ACTIVE")
							.stream()
							.map(membership -> membership.fiscalRole())
							.distinct()
							.toList();
					List<SimpleGrantedAuthority> authorities = roles.stream()
							.map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
							.toList();
					CurrentUser principal = new CurrentUser(user.id(), user.email(), user.displayName(), roles);
					return new UsernamePasswordAuthenticationToken(principal, credentials, authorities);
				})
				.orElseThrow(() -> new UnknownApplicationUserException("No active application user exists for " + email));
	}

	static class UnknownApplicationUserException extends AuthenticationException {
		UnknownApplicationUserException(String message) {
			super(message);
		}
	}
}

package com.fiscalsaas.backend.security;

import java.util.List;

import com.fiscalsaas.backend.identity.FiscalRole;

public record CurrentUser(String id, String email, String displayName, List<FiscalRole> roles) {

	public boolean hasRole(FiscalRole role) {
		return roles.contains(role);
	}
}

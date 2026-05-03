package com.fiscalsaas.backend.identity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, String> {
	Optional<AppUser> findByEmailIgnoreCaseAndStatus(String email, String status);

	boolean existsByEmailIgnoreCase(String email);
}

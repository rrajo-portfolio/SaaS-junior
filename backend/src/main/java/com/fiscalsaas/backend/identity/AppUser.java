package com.fiscalsaas.backend.identity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class AppUser {

	@Id
	@Column(length = 36, nullable = false)
	private String id;

	@Column(nullable = false, unique = true, length = 180)
	private String email;

	@Column(name = "display_name", nullable = false, length = 180)
	private String displayName;

	@Column(nullable = false, length = 30)
	private String status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected AppUser() {
	}

	public String id() {
		return id;
	}

	public String email() {
		return email;
	}

	public String displayName() {
		return displayName;
	}

	public String status() {
		return status;
	}
}

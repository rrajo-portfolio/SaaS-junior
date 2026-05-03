package com.fiscalsaas.backend.saas;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

	@Id
	@Column(length = 40, nullable = false)
	private String code;

	@Column(name = "display_name", nullable = false, length = 120)
	private String displayName;

	@Column(nullable = false, length = 30)
	private String status;

	@Column(name = "monthly_price_cents", nullable = false)
	private int monthlyPriceCents;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "max_users", nullable = false)
	private int maxUsers;

	@Column(name = "max_documents", nullable = false)
	private int maxDocuments;

	@Column(name = "max_invoices", nullable = false)
	private int maxInvoices;

	@Column(name = "includes_verifactu", nullable = false)
	private boolean includesVerifactu;

	@Column(name = "includes_einvoice", nullable = false)
	private boolean includesEinvoice;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected SubscriptionPlan() {
	}

	public String code() {
		return code;
	}

	public String displayName() {
		return displayName;
	}

	public String status() {
		return status;
	}

	public int monthlyPriceCents() {
		return monthlyPriceCents;
	}

	public String currency() {
		return currency;
	}

	public int maxUsers() {
		return maxUsers;
	}

	public int maxDocuments() {
		return maxDocuments;
	}

	public int maxInvoices() {
		return maxInvoices;
	}

	public boolean includesVerifactu() {
		return includesVerifactu;
	}

	public boolean includesEinvoice() {
		return includesEinvoice;
	}
}

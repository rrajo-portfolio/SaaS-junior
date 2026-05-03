package com.fiscalsaas.backend.saas;

public record SubscriptionPlanResponse(
		String code,
		String displayName,
		String status,
		int monthlyPriceCents,
		String currency,
		int maxUsers,
		int maxDocuments,
		int maxInvoices,
		boolean includesVerifactu,
		boolean includesEinvoice) {

	static SubscriptionPlanResponse from(SubscriptionPlan plan) {
		return new SubscriptionPlanResponse(
				plan.code(),
				plan.displayName(),
				plan.status(),
				plan.monthlyPriceCents(),
				plan.currency(),
				plan.maxUsers(),
				plan.maxDocuments(),
				plan.maxInvoices(),
				plan.includesVerifactu(),
				plan.includesEinvoice());
	}
}

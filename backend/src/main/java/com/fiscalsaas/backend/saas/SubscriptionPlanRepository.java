package com.fiscalsaas.backend.saas;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {
	List<SubscriptionPlan> findByStatusOrderByMonthlyPriceCentsAsc(String status);
}

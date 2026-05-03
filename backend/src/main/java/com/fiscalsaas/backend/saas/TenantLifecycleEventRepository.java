package com.fiscalsaas.backend.saas;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantLifecycleEventRepository extends JpaRepository<TenantLifecycleEvent, String> {
	@EntityGraph(attributePaths = {"tenant", "actor"})
	List<TenantLifecycleEvent> findByTenantIdOrderByCreatedAtDesc(String tenantId);
}

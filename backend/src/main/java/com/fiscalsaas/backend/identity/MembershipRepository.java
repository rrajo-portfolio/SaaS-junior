package com.fiscalsaas.backend.identity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, String> {
	@EntityGraph(attributePaths = {"tenant", "user"})
	List<Membership> findByUserIdAndStatusOrderByTenantDisplayNameAsc(String userId, String status);

	@EntityGraph(attributePaths = {"tenant", "user"})
	Optional<Membership> findByTenantIdAndUserIdAndStatus(String tenantId, String userId, String status);
}

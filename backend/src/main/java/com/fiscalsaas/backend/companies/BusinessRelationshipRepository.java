package com.fiscalsaas.backend.companies;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessRelationshipRepository extends JpaRepository<BusinessRelationship, String> {
	@EntityGraph(attributePaths = {"tenant", "sourceCompany", "targetCompany"})
	List<BusinessRelationship> findByTenant_IdOrderByCreatedAtDesc(String tenantId);

	@EntityGraph(attributePaths = {"tenant", "sourceCompany", "targetCompany"})
	Optional<BusinessRelationship> findByIdAndTenant_Id(String id, String tenantId);

	boolean existsByTenant_IdAndSourceCompany_IdAndTargetCompany_IdAndRelationshipKind(
			String tenantId,
			String sourceCompanyId,
			String targetCompanyId,
			String relationshipKind);
}

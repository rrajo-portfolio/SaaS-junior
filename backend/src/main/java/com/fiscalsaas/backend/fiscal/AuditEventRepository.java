package com.fiscalsaas.backend.fiscal;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditEventRepository extends JpaRepository<AuditEvent, String> {
	@EntityGraph(attributePaths = {"actor", "company"})
	List<AuditEvent> findByTenant_IdAndCompany_IdOrderByOccurredAtDesc(String tenantId, String companyId);

	@EntityGraph(attributePaths = {"actor", "company"})
	List<AuditEvent> findByTenant_IdAndEntityTypeAndEntityIdOrderByOccurredAtDesc(String tenantId, String entityType, String entityId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select event from AuditEvent event where event.tenant.id = :tenantId and event.company.id = :companyId order by event.occurredAt desc")
	List<AuditEvent> findLastForCompanyForUpdate(@Param("tenantId") String tenantId, @Param("companyId") String companyId, Pageable pageable);

	Optional<AuditEvent> findFirstByTenant_IdOrderByOccurredAtDesc(String tenantId);
}

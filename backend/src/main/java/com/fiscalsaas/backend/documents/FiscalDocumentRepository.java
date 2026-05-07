package com.fiscalsaas.backend.documents;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FiscalDocumentRepository extends JpaRepository<FiscalDocument, String> {
	@EntityGraph(attributePaths = {"tenant", "company"})
	List<FiscalDocument> findByTenant_IdOrderByUpdatedAtDesc(String tenantId);

	@EntityGraph(attributePaths = {"tenant", "company"})
	List<FiscalDocument> findByTenant_IdAndCompany_IdOrderByUpdatedAtDesc(String tenantId, String companyId);

	@EntityGraph(attributePaths = {"tenant", "company"})
	Optional<FiscalDocument> findByIdAndTenant_Id(String id, String tenantId);
}

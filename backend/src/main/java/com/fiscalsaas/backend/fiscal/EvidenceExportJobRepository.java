package com.fiscalsaas.backend.fiscal;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceExportJobRepository extends JpaRepository<EvidenceExportJob, String> {
	List<EvidenceExportJob> findByTenant_IdAndCompany_IdOrderByCreatedAtDesc(String tenantId, String companyId);

	Optional<EvidenceExportJob> findByIdAndTenant_IdAndCompany_Id(String id, String tenantId, String companyId);
}

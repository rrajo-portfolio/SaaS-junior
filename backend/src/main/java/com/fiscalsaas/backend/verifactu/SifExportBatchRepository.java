package com.fiscalsaas.backend.verifactu;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SifExportBatchRepository extends JpaRepository<SifExportBatch, String> {
	List<SifExportBatch> findByTenant_IdOrderByCreatedAtDesc(String tenantId);

	Optional<SifExportBatch> findByIdAndTenant_Id(String id, String tenantId);
}

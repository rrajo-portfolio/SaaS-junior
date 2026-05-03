package com.fiscalsaas.backend.verifactu;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SifRecordRepository extends JpaRepository<SifRecord, String> {

	@EntityGraph(attributePaths = {"tenant", "invoice", "invoice.issuerCompany", "invoice.customerCompany"})
	List<SifRecord> findByTenant_IdOrderBySequenceNumberDesc(String tenantId);

	@EntityGraph(attributePaths = {"tenant", "invoice", "invoice.issuerCompany", "invoice.customerCompany", "sourceRecord"})
	List<SifRecord> findByTenant_IdOrderBySequenceNumberAsc(String tenantId);

	@EntityGraph(attributePaths = {"tenant", "invoice", "invoice.issuerCompany", "invoice.customerCompany", "sourceRecord"})
	Optional<SifRecord> findByIdAndTenant_Id(String id, String tenantId);

	boolean existsByTenant_IdAndInvoice_IdAndRecordType(String tenantId, String invoiceId, String recordType);

	boolean existsByTenant_IdAndSourceRecord_IdAndRecordType(String tenantId, String sourceRecordId, String recordType);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select record from SifRecord record where record.tenant.id = :tenantId order by record.sequenceNumber desc")
	List<SifRecord> findLastForTenantForUpdate(@Param("tenantId") String tenantId, Pageable pageable);
}

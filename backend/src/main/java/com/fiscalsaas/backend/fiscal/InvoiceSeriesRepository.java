package com.fiscalsaas.backend.fiscal;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface InvoiceSeriesRepository extends JpaRepository<InvoiceSeries, String> {
	List<InvoiceSeries> findByCompany_IdAndTenant_IdOrderByCodeAsc(String companyId, String tenantId);

	Optional<InvoiceSeries> findFirstByCompany_IdAndTenant_IdAndActiveTrueOrderByCodeAsc(String companyId, String tenantId);

	Optional<InvoiceSeries> findByIdAndCompany_IdAndTenant_Id(String id, String companyId, String tenantId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<InvoiceSeries> findByIdAndTenant_Id(String id, String tenantId);

	boolean existsByCompany_IdAndTenant_IdAndCode(String companyId, String tenantId, String code);
}

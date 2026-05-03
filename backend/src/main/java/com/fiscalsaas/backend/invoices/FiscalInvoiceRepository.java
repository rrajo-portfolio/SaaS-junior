package com.fiscalsaas.backend.invoices;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FiscalInvoiceRepository extends JpaRepository<FiscalInvoice, String> {
	@EntityGraph(attributePaths = {"tenant", "issuerCompany", "customerCompany"})
	List<FiscalInvoice> findByTenant_IdOrderByIssueDateDescInvoiceNumberDesc(String tenantId);

	@EntityGraph(attributePaths = {"tenant", "issuerCompany", "customerCompany"})
	Optional<FiscalInvoice> findByIdAndTenant_Id(String id, String tenantId);

	boolean existsByTenant_IdAndInvoiceNumber(String tenantId, String invoiceNumber);
}

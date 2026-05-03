package com.fiscalsaas.backend.einvoice;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EInvoiceMessageRepository extends JpaRepository<EInvoiceMessage, String> {
	@EntityGraph(attributePaths = {"tenant", "invoice", "invoice.issuerCompany", "invoice.customerCompany"})
	List<EInvoiceMessage> findByTenant_IdOrderByCreatedAtDesc(String tenantId);

	@EntityGraph(attributePaths = {"tenant", "invoice", "invoice.issuerCompany", "invoice.customerCompany"})
	Optional<EInvoiceMessage> findByIdAndTenant_Id(String id, String tenantId);

	boolean existsByTenant_IdAndInvoice_Id(String tenantId, String invoiceId);
}

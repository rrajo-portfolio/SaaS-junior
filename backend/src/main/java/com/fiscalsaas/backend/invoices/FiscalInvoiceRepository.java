package com.fiscalsaas.backend.invoices;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FiscalInvoiceRepository extends JpaRepository<FiscalInvoice, String> {
	@EntityGraph(attributePaths = {"tenant", "issuerCompany", "customerCompany"})
	List<FiscalInvoice> findByTenant_IdOrderByIssueDateDescInvoiceNumberDesc(String tenantId);

	@EntityGraph(attributePaths = {"tenant", "issuerCompany", "customerCompany"})
	@Query("""
			select invoice
			from FiscalInvoice invoice
			where invoice.tenant.id = :tenantId
				and (
					:companyId is null
					or invoice.issuerCompany.id = :companyId
					or invoice.customerCompany.id = :companyId
				)
				and (
					:status is null
					or invoice.status = :status
				)
				and (
					:search is null
					or lower(invoice.invoiceNumber) like lower(concat('%', :search, '%'))
					or lower(invoice.issuerCompany.legalName) like lower(concat('%', :search, '%'))
					or lower(invoice.customerCompany.legalName) like lower(concat('%', :search, '%'))
				)
			order by invoice.issueDate desc, invoice.invoiceNumber desc
			""")
	List<FiscalInvoice> searchByTenant(
			@Param("tenantId") String tenantId,
			@Param("companyId") String companyId,
			@Param("status") String status,
			@Param("search") String search);

	@EntityGraph(attributePaths = {"tenant", "issuerCompany", "customerCompany"})
	Optional<FiscalInvoice> findByIdAndTenant_Id(String id, String tenantId);

	boolean existsByTenant_IdAndInvoiceNumber(String tenantId, String invoiceNumber);

	boolean existsByTenant_IdAndInvoiceNumberAndIdNot(String tenantId, String invoiceNumber, String id);

	boolean existsByTenant_IdAndSeriesCodeAndFiscalNumberIsNotNull(String tenantId, String seriesCode);

	boolean existsByTenant_IdAndIssueRequestId(String tenantId, String issueRequestId);
}

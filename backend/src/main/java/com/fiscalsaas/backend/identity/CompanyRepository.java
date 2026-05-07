package com.fiscalsaas.backend.identity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyRepository extends JpaRepository<Company, String> {
	@EntityGraph(attributePaths = "tenant")
	List<Company> findByTenantIdOrderByLegalNameAsc(String tenantId);

	@EntityGraph(attributePaths = "tenant")
	@Query("""
			select company
			from Company company
			where company.tenant.id = :tenantId
				and (
					:search is null
					or lower(company.legalName) like lower(concat('%', :search, '%'))
					or lower(company.taxId) like lower(concat('%', :search, '%'))
					or lower(company.relationshipType) like lower(concat('%', :search, '%'))
					or lower(company.status) like lower(concat('%', :search, '%'))
				)
			order by company.legalName asc
			""")
	List<Company> searchByTenant(
			@Param("tenantId") String tenantId,
			@Param("search") String search);

	@EntityGraph(attributePaths = "tenant")
	Optional<Company> findByIdAndTenantId(String id, String tenantId);

	boolean existsByTenantIdAndTaxId(String tenantId, String taxId);

	boolean existsByTenantIdAndTaxIdAndIdNot(String tenantId, String taxId, String id);
}

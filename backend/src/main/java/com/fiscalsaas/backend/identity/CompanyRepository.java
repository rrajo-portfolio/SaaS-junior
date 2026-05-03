package com.fiscalsaas.backend.identity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, String> {
	@EntityGraph(attributePaths = "tenant")
	List<Company> findByTenantIdOrderByLegalNameAsc(String tenantId);

	@EntityGraph(attributePaths = "tenant")
	Optional<Company> findByIdAndTenantId(String id, String tenantId);

	boolean existsByTenantIdAndTaxId(String tenantId, String taxId);

	boolean existsByTenantIdAndTaxIdAndIdNot(String tenantId, String taxId, String id);
}

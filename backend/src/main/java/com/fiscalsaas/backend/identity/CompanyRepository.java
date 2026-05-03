package com.fiscalsaas.backend.identity;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, String> {
	@EntityGraph(attributePaths = "tenant")
	List<Company> findByTenantIdOrderByLegalNameAsc(String tenantId);
}

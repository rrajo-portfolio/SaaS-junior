package com.fiscalsaas.backend.fiscal;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyFiscalSettingsRepository extends JpaRepository<CompanyFiscalSettings, String> {
	Optional<CompanyFiscalSettings> findByCompany_IdAndTenant_Id(String companyId, String tenantId);

	boolean existsByCompany_IdAndTenant_Id(String companyId, String tenantId);
}

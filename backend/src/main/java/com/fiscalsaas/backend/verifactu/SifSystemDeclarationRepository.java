package com.fiscalsaas.backend.verifactu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SifSystemDeclarationRepository extends JpaRepository<SifSystemDeclaration, String> {
	List<SifSystemDeclaration> findByTenant_IdOrderByCreatedAtDesc(String tenantId);
}

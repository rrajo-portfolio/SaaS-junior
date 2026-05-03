package com.fiscalsaas.backend.verifactu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SifTransmissionAttemptRepository extends JpaRepository<SifTransmissionAttempt, String> {
	List<SifTransmissionAttempt> findByTenant_IdAndRecord_IdOrderByCreatedAtDesc(String tenantId, String recordId);
}

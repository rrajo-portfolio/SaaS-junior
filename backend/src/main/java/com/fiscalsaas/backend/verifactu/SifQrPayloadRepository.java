package com.fiscalsaas.backend.verifactu;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SifQrPayloadRepository extends JpaRepository<SifQrPayload, String> {
	Optional<SifQrPayload> findByTenant_IdAndRecord_Id(String tenantId, String recordId);
}

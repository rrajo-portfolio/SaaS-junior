package com.fiscalsaas.backend.verifactu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SifEventLogRepository extends JpaRepository<SifEventLog, String> {
	List<SifEventLog> findByTenant_IdAndRecord_IdOrderByEventAtAsc(String tenantId, String recordId);
}

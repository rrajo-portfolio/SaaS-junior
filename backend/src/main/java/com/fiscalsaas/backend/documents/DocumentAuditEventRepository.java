package com.fiscalsaas.backend.documents;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentAuditEventRepository extends JpaRepository<DocumentAuditEvent, String> {
	List<DocumentAuditEvent> findByDocument_IdOrderByEventAtDesc(String documentId);
}

package com.fiscalsaas.backend.einvoice;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EInvoiceEventRepository extends JpaRepository<EInvoiceEvent, String> {
	List<EInvoiceEvent> findByTenant_IdAndMessage_IdOrderByEventAtDesc(String tenantId, String messageId);
}

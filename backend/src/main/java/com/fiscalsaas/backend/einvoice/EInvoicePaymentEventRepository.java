package com.fiscalsaas.backend.einvoice;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EInvoicePaymentEventRepository extends JpaRepository<EInvoicePaymentEvent, String> {
	List<EInvoicePaymentEvent> findByTenant_IdAndMessage_IdOrderByCreatedAtDesc(String tenantId, String messageId);
}

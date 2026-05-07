package com.fiscalsaas.backend.invoices;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoicePaymentRepository extends JpaRepository<InvoicePayment, String> {
	List<InvoicePayment> findByTenant_IdAndInvoice_IdOrderByPaymentDateDescCreatedAtDesc(String tenantId, String invoiceId);
}

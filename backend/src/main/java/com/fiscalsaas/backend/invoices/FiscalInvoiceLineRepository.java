package com.fiscalsaas.backend.invoices;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FiscalInvoiceLineRepository extends JpaRepository<FiscalInvoiceLine, String> {
	List<FiscalInvoiceLine> findByInvoice_IdOrderByLineNumberAsc(String invoiceId);
}

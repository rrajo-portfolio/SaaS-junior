package com.fiscalsaas.backend.invoices;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FiscalInvoiceTaxRepository extends JpaRepository<FiscalInvoiceTax, String> {
	List<FiscalInvoiceTax> findByInvoice_IdOrderByTaxRateAsc(String invoiceId);
}

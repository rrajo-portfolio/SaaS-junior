package com.fiscalsaas.backend.invoices;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceArtifactRepository extends JpaRepository<InvoiceArtifact, String> {
}

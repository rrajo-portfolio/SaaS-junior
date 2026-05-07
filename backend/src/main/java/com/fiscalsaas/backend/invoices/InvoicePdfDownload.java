package com.fiscalsaas.backend.invoices;

public record InvoicePdfDownload(String filename, byte[] bytes, String sha256) {
}

package com.fiscalsaas.backend.fiscal;

public record EvidenceExportDownload(String filename, byte[] bytes, String sha256) {
}

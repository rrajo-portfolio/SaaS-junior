package com.fiscalsaas.backend.documents;

public record StoredDocument(String storageKey, String sha256, long byteSize, String contentType, String originalFilename) {
}

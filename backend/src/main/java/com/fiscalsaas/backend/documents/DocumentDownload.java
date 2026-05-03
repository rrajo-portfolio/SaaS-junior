package com.fiscalsaas.backend.documents;

import org.springframework.core.io.Resource;

public record DocumentDownload(Resource resource, DocumentVersion version) {
}

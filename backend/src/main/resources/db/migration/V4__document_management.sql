CREATE TABLE fiscal_documents (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    company_id VARCHAR(36) NOT NULL,
    document_type VARCHAR(60) NOT NULL,
    title VARCHAR(220) NOT NULL,
    status VARCHAR(30) NOT NULL,
    current_version INT NOT NULL,
    created_by_user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_fiscal_documents PRIMARY KEY (id),
    CONSTRAINT fk_fiscal_documents_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_fiscal_documents_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_fiscal_documents_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_users (id)
);

CREATE TABLE document_versions (
    id VARCHAR(36) NOT NULL,
    document_id VARCHAR(36) NOT NULL,
    version_number INT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    byte_size BIGINT NOT NULL,
    sha256 VARCHAR(64) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    uploaded_by_user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_document_versions PRIMARY KEY (id),
    CONSTRAINT fk_document_versions_document FOREIGN KEY (document_id) REFERENCES fiscal_documents (id),
    CONSTRAINT fk_document_versions_uploaded_by FOREIGN KEY (uploaded_by_user_id) REFERENCES app_users (id),
    CONSTRAINT uk_document_versions_document_version UNIQUE (document_id, version_number),
    CONSTRAINT uk_document_versions_storage_key UNIQUE (storage_key)
);

CREATE TABLE document_audit_events (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    document_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(60) NOT NULL,
    actor_user_id VARCHAR(36) NOT NULL,
    event_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    details VARCHAR(600),
    CONSTRAINT pk_document_audit_events PRIMARY KEY (id),
    CONSTRAINT fk_document_audit_events_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_document_audit_events_document FOREIGN KEY (document_id) REFERENCES fiscal_documents (id),
    CONSTRAINT fk_document_audit_events_actor FOREIGN KEY (actor_user_id) REFERENCES app_users (id)
);

CREATE INDEX ix_fiscal_documents_tenant_status ON fiscal_documents (tenant_id, status, updated_at);
CREATE INDEX ix_fiscal_documents_company ON fiscal_documents (company_id);
CREATE INDEX ix_document_versions_document ON document_versions (document_id, version_number);
CREATE INDEX ix_document_audit_events_document ON document_audit_events (document_id, event_at);

CREATE TABLE sif_records (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    invoice_id VARCHAR(36) NOT NULL,
    source_record_id VARCHAR(36),
    record_type VARCHAR(40) NOT NULL,
    sequence_number BIGINT NOT NULL,
    previous_hash VARCHAR(64) NOT NULL,
    record_hash VARCHAR(64) NOT NULL,
    canonical_payload VARCHAR(4000) NOT NULL,
    system_version VARCHAR(40) NOT NULL,
    normative_version VARCHAR(80) NOT NULL,
    created_by_user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_sif_records PRIMARY KEY (id),
    CONSTRAINT fk_sif_records_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_sif_records_invoice FOREIGN KEY (invoice_id) REFERENCES fiscal_invoices (id),
    CONSTRAINT fk_sif_records_source FOREIGN KEY (source_record_id) REFERENCES sif_records (id),
    CONSTRAINT fk_sif_records_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_users (id),
    CONSTRAINT uk_sif_records_tenant_sequence UNIQUE (tenant_id, sequence_number),
    CONSTRAINT uk_sif_records_hash UNIQUE (record_hash),
    CONSTRAINT uk_sif_records_invoice_type UNIQUE (tenant_id, invoice_id, record_type),
    CONSTRAINT uk_sif_records_source_type UNIQUE (tenant_id, source_record_id, record_type)
);

CREATE TABLE sif_record_hash_chain (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    record_id VARCHAR(36) NOT NULL,
    sequence_number BIGINT NOT NULL,
    previous_hash VARCHAR(64) NOT NULL,
    record_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_sif_record_hash_chain PRIMARY KEY (id),
    CONSTRAINT fk_sif_hash_chain_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_sif_hash_chain_record FOREIGN KEY (record_id) REFERENCES sif_records (id),
    CONSTRAINT uk_sif_hash_chain_record UNIQUE (record_id),
    CONSTRAINT uk_sif_hash_chain_tenant_sequence UNIQUE (tenant_id, sequence_number)
);

CREATE TABLE sif_event_log (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    record_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(60) NOT NULL,
    actor_user_id VARCHAR(36) NOT NULL,
    event_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    details VARCHAR(600),
    CONSTRAINT pk_sif_event_log PRIMARY KEY (id),
    CONSTRAINT fk_sif_event_log_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_sif_event_log_record FOREIGN KEY (record_id) REFERENCES sif_records (id),
    CONSTRAINT fk_sif_event_log_actor FOREIGN KEY (actor_user_id) REFERENCES app_users (id)
);

CREATE TABLE sif_export_batches (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    record_from_sequence BIGINT NOT NULL,
    record_to_sequence BIGINT NOT NULL,
    record_count INT NOT NULL,
    export_sha256 VARCHAR(64) NOT NULL,
    payload VARCHAR(10000) NOT NULL,
    created_by_user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_sif_export_batches PRIMARY KEY (id),
    CONSTRAINT fk_sif_export_batches_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_sif_export_batches_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_users (id),
    CONSTRAINT uk_sif_export_batches_hash UNIQUE (export_sha256)
);

CREATE INDEX ix_sif_records_tenant_type ON sif_records (tenant_id, record_type, sequence_number);
CREATE INDEX ix_sif_records_invoice ON sif_records (invoice_id);
CREATE INDEX ix_sif_event_log_record ON sif_event_log (record_id, event_at);
CREATE INDEX ix_sif_export_batches_tenant ON sif_export_batches (tenant_id, created_at);

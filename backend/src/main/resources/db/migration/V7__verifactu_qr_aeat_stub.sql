CREATE TABLE sif_qr_payloads (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    record_id VARCHAR(36) NOT NULL,
    qr_payload VARCHAR(1000) NOT NULL,
    qr_sha256 VARCHAR(64) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_sif_qr_payloads PRIMARY KEY (id),
    CONSTRAINT fk_sif_qr_payloads_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_sif_qr_payloads_record FOREIGN KEY (record_id) REFERENCES sif_records (id),
    CONSTRAINT uk_sif_qr_payloads_record UNIQUE (record_id),
    CONSTRAINT uk_sif_qr_payloads_hash UNIQUE (qr_sha256)
);

CREATE TABLE sif_transmission_attempts (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    record_id VARCHAR(36) NOT NULL,
    mode VARCHAR(30) NOT NULL,
    status VARCHAR(40) NOT NULL,
    request_payload VARCHAR(4000) NOT NULL,
    response_payload VARCHAR(4000) NOT NULL,
    created_by_user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_sif_transmission_attempts PRIMARY KEY (id),
    CONSTRAINT fk_sif_transmission_attempts_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_sif_transmission_attempts_record FOREIGN KEY (record_id) REFERENCES sif_records (id),
    CONSTRAINT fk_sif_transmission_attempts_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_users (id)
);

CREATE TABLE sif_system_declarations (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL,
    payload VARCHAR(5000) NOT NULL,
    payload_sha256 VARCHAR(64) NOT NULL,
    created_by_user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_sif_system_declarations PRIMARY KEY (id),
    CONSTRAINT fk_sif_system_declarations_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_sif_system_declarations_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_users (id),
    CONSTRAINT uk_sif_system_declarations_hash UNIQUE (payload_sha256)
);

CREATE INDEX ix_sif_transmission_attempts_record ON sif_transmission_attempts (record_id, created_at);
CREATE INDEX ix_sif_system_declarations_tenant ON sif_system_declarations (tenant_id, created_at);

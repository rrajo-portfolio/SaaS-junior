CREATE TABLE einvoice_messages (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    invoice_id VARCHAR(36) NOT NULL,
    syntax VARCHAR(30) NOT NULL,
    direction VARCHAR(30) NOT NULL,
    exchange_status VARCHAR(30) NOT NULL,
    commercial_status VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    payload VARCHAR(12000) NOT NULL,
    payload_sha256 VARCHAR(64) NOT NULL,
    status_reason VARCHAR(500),
    created_by_user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_einvoice_messages PRIMARY KEY (id),
    CONSTRAINT fk_einvoice_messages_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_einvoice_messages_invoice FOREIGN KEY (invoice_id) REFERENCES fiscal_invoices (id),
    CONSTRAINT fk_einvoice_messages_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_users (id),
    CONSTRAINT uk_einvoice_messages_invoice UNIQUE (invoice_id),
    CONSTRAINT uk_einvoice_messages_payload_hash UNIQUE (payload_sha256)
);

CREATE TABLE einvoice_events (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    message_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    details VARCHAR(2000),
    event_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by_user_id VARCHAR(36) NOT NULL,
    CONSTRAINT pk_einvoice_events PRIMARY KEY (id),
    CONSTRAINT fk_einvoice_events_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_einvoice_events_message FOREIGN KEY (message_id) REFERENCES einvoice_messages (id),
    CONSTRAINT fk_einvoice_events_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_users (id)
);

CREATE TABLE einvoice_payment_events (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    message_id VARCHAR(36) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    paid_at TIMESTAMP(6) NOT NULL,
    payment_reference VARCHAR(120),
    notes VARCHAR(500),
    created_by_user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_einvoice_payment_events PRIMARY KEY (id),
    CONSTRAINT fk_einvoice_payment_events_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_einvoice_payment_events_message FOREIGN KEY (message_id) REFERENCES einvoice_messages (id),
    CONSTRAINT fk_einvoice_payment_events_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_users (id)
);

CREATE INDEX ix_einvoice_messages_tenant_status ON einvoice_messages (tenant_id, exchange_status, commercial_status, payment_status, created_at);
CREATE INDEX ix_einvoice_events_message ON einvoice_events (message_id, event_at);
CREATE INDEX ix_einvoice_payment_events_message ON einvoice_payment_events (message_id, paid_at);

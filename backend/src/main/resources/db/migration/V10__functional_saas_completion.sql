CREATE TABLE company_fiscal_settings (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    company_id VARCHAR(36) NOT NULL,
    legal_name VARCHAR(220) NOT NULL,
    trade_name VARCHAR(220),
    nif VARCHAR(40) NOT NULL,
    vat_number VARCHAR(40),
    address_line1 VARCHAR(220) NOT NULL,
    address_line2 VARCHAR(220),
    city VARCHAR(120) NOT NULL,
    province VARCHAR(120),
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(2) NOT NULL,
    default_currency VARCHAR(3) NOT NULL,
    default_payment_terms_days INT NOT NULL,
    default_vat_rate DECIMAL(5, 2) NOT NULL,
    default_language VARCHAR(8) NOT NULL,
    pdf_template VARCHAR(80) NOT NULL,
    sif_mode VARCHAR(30) NOT NULL,
    verifactu_label_enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_company_fiscal_settings PRIMARY KEY (id),
    CONSTRAINT fk_company_fiscal_settings_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_company_fiscal_settings_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT uk_company_fiscal_settings_company UNIQUE (company_id)
);

CREATE TABLE invoice_series (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    company_id VARCHAR(36) NOT NULL,
    code VARCHAR(40) NOT NULL,
    prefix VARCHAR(80) NOT NULL,
    next_number BIGINT NOT NULL,
    padding INT NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_invoice_series PRIMARY KEY (id),
    CONSTRAINT fk_invoice_series_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_invoice_series_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT uk_invoice_series_company_code UNIQUE (company_id, code)
);

CREATE TABLE customers (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    company_id VARCHAR(36) NOT NULL,
    customer_type VARCHAR(30) NOT NULL,
    name VARCHAR(220) NOT NULL,
    nif VARCHAR(40) NOT NULL,
    vat_number VARCHAR(40),
    email VARCHAR(180),
    phone VARCHAR(40),
    address_line1 VARCHAR(220) NOT NULL,
    address_line2 VARCHAR(220),
    city VARCHAR(120) NOT NULL,
    province VARCHAR(120),
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT fk_customers_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_customers_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT uk_customers_company_nif UNIQUE (company_id, nif)
);

CREATE TABLE audit_events (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    company_id VARCHAR(36),
    actor_user_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    entity_type VARCHAR(60) NOT NULL,
    entity_id VARCHAR(36) NOT NULL,
    details VARCHAR(2000),
    previous_hash VARCHAR(64) NOT NULL,
    event_hash VARCHAR(64) NOT NULL,
    occurred_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_audit_events PRIMARY KEY (id),
    CONSTRAINT fk_audit_events_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_audit_events_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_audit_events_actor FOREIGN KEY (actor_user_id) REFERENCES app_users (id),
    CONSTRAINT uk_audit_events_hash UNIQUE (event_hash)
);

CREATE TABLE invoice_artifacts (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    company_id VARCHAR(36) NOT NULL,
    invoice_id VARCHAR(36) NOT NULL,
    artifact_type VARCHAR(30) NOT NULL,
    filename VARCHAR(220) NOT NULL,
    sha256 VARCHAR(64) NOT NULL,
    generated_by_user_id VARCHAR(36) NOT NULL,
    generated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_invoice_artifacts PRIMARY KEY (id),
    CONSTRAINT fk_invoice_artifacts_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_invoice_artifacts_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_invoice_artifacts_invoice FOREIGN KEY (invoice_id) REFERENCES fiscal_invoices (id),
    CONSTRAINT fk_invoice_artifacts_generated_by FOREIGN KEY (generated_by_user_id) REFERENCES app_users (id)
);

CREATE TABLE invoice_payments (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    company_id VARCHAR(36) NOT NULL,
    invoice_id VARCHAR(36) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    payment_date DATE NOT NULL,
    method VARCHAR(40) NOT NULL,
    reference VARCHAR(120),
    notes VARCHAR(500),
    created_by_user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_invoice_payments PRIMARY KEY (id),
    CONSTRAINT fk_invoice_payments_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_invoice_payments_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_invoice_payments_invoice FOREIGN KEY (invoice_id) REFERENCES fiscal_invoices (id),
    CONSTRAINT fk_invoice_payments_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_users (id)
);

CREATE TABLE export_jobs (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    company_id VARCHAR(36) NOT NULL,
    requested_by_user_id VARCHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL,
    export_type VARCHAR(40) NOT NULL,
    filters_json VARCHAR(2000),
    sha256 VARCHAR(64) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    completed_at TIMESTAMP(6),
    CONSTRAINT pk_export_jobs PRIMARY KEY (id),
    CONSTRAINT fk_export_jobs_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_export_jobs_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_export_jobs_requested_by FOREIGN KEY (requested_by_user_id) REFERENCES app_users (id)
);

CREATE TABLE job_runs (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36),
    company_id VARCHAR(36),
    job_type VARCHAR(80) NOT NULL,
    status VARCHAR(30) NOT NULL,
    started_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    finished_at TIMESTAMP(6),
    error_message VARCHAR(1000),
    metadata_json VARCHAR(2000),
    CONSTRAINT pk_job_runs PRIMARY KEY (id),
    CONSTRAINT fk_job_runs_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_job_runs_company FOREIGN KEY (company_id) REFERENCES companies (id)
);

ALTER TABLE fiscal_invoices ADD COLUMN customer_id VARCHAR(36);
ALTER TABLE fiscal_invoices ADD COLUMN customer_snapshot VARCHAR(2000);
ALTER TABLE fiscal_invoices ADD COLUMN issuer_fiscal_snapshot VARCHAR(2000);
ALTER TABLE fiscal_invoices ADD COLUMN series_id VARCHAR(36);
ALTER TABLE fiscal_invoices ADD COLUMN series_code VARCHAR(40);
ALTER TABLE fiscal_invoices ADD COLUMN fiscal_number VARCHAR(80);
ALTER TABLE fiscal_invoices ADD COLUMN issued_at TIMESTAMP(6);
ALTER TABLE fiscal_invoices ADD COLUMN issue_request_id VARCHAR(80);
ALTER TABLE fiscal_invoices ADD COLUMN due_date DATE;
ALTER TABLE fiscal_invoices ADD COLUMN payment_status VARCHAR(30) NOT NULL DEFAULT 'UNPAID';
ALTER TABLE fiscal_invoices ADD COLUMN paid_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE fiscal_invoices ADD COLUMN outstanding_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE fiscal_invoices ADD COLUMN withholding_total DECIMAL(19, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE fiscal_invoices ADD COLUMN gross_total DECIMAL(19, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE fiscal_invoices ADD COLUMN net_total DECIMAL(19, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE fiscal_invoices ADD COLUMN payable_total DECIMAL(19, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE fiscal_invoices ADD COLUMN totals_snapshot VARCHAR(2000);
ALTER TABLE fiscal_invoices ADD COLUMN cancellation_reason VARCHAR(500);
ALTER TABLE fiscal_invoices ADD COLUMN cancelled_at TIMESTAMP(6);

ALTER TABLE fiscal_invoice_lines ADD COLUMN discount_percent DECIMAL(5, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE fiscal_invoice_lines ADD COLUMN withholding_percent DECIMAL(5, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE fiscal_invoice_lines ADD COLUMN withholding_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE fiscal_invoice_lines ADD COLUMN tax_category VARCHAR(40) NOT NULL DEFAULT 'STANDARD';

ALTER TABLE fiscal_invoices ADD CONSTRAINT fk_fiscal_invoices_customer_record FOREIGN KEY (customer_id) REFERENCES customers (id);
ALTER TABLE fiscal_invoices ADD CONSTRAINT fk_fiscal_invoices_series FOREIGN KEY (series_id) REFERENCES invoice_series (id);
ALTER TABLE fiscal_invoices ADD CONSTRAINT uk_fiscal_invoices_series_number UNIQUE (tenant_id, series_code, fiscal_number);
ALTER TABLE fiscal_invoices ADD CONSTRAINT uk_fiscal_invoices_issue_request UNIQUE (tenant_id, issue_request_id);

UPDATE fiscal_invoices SET
    gross_total = taxable_base,
    net_total = taxable_base,
    payable_total = total,
    outstanding_amount = CASE WHEN status = 'ISSUED' THEN total ELSE 0.00 END;

CREATE INDEX ix_company_fiscal_settings_tenant ON company_fiscal_settings (tenant_id, company_id);
CREATE INDEX ix_invoice_series_company ON invoice_series (company_id, active);
CREATE INDEX ix_customers_company_search ON customers (company_id, status, name);
CREATE INDEX ix_customers_company_nif ON customers (company_id, nif);
CREATE INDEX ix_audit_events_company_time ON audit_events (company_id, occurred_at);
CREATE INDEX ix_audit_events_entity ON audit_events (tenant_id, entity_type, entity_id);
CREATE INDEX ix_invoice_artifacts_invoice ON invoice_artifacts (invoice_id, artifact_type);
CREATE INDEX ix_invoice_payments_invoice ON invoice_payments (invoice_id, payment_date);
CREATE INDEX ix_export_jobs_company ON export_jobs (company_id, created_at);
CREATE INDEX ix_job_runs_status ON job_runs (status, started_at);

INSERT INTO company_fiscal_settings (
    id, tenant_id, company_id, legal_name, trade_name, nif, vat_number, address_line1, address_line2,
    city, province, postal_code, country, default_currency, default_payment_terms_days,
    default_vat_rate, default_language, pdf_template, sif_mode, verifactu_label_enabled
)
SELECT
    CONCAT('50000000-0000-0000-0000-', RIGHT(REPLACE(id, '-', ''), 12)),
    tenant_id,
    id,
    legal_name,
    legal_name,
    tax_id,
    tax_id,
    'Calle Fiscal 1',
    NULL,
    'Madrid',
    'Madrid',
    '28001',
    country_code,
    'EUR',
    30,
    21.00,
    'es',
    'standard',
    'LOCAL_ONLY',
    FALSE
FROM companies;

INSERT INTO invoice_series (
    id, tenant_id, company_id, code, prefix, next_number, padding, active
)
SELECT
    CONCAT('51000000-0000-0000-0000-', RIGHT(REPLACE(id, '-', ''), 12)),
    tenant_id,
    id,
    '2026',
    'F-2026-',
    1,
    6,
    TRUE
FROM companies;

INSERT INTO customers (
    id, tenant_id, company_id, customer_type, name, nif, vat_number, email, phone,
    address_line1, address_line2, city, province, postal_code, country, status
)
VALUES
    ('52000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 'COMPANY', 'Alba Retail Group SL', 'B87654321', 'B87654321', 'facturas@alba.local', NULL, 'Avenida Cliente 10', NULL, 'Madrid', 'Madrid', '28002', 'ES', 'ACTIVE'),
    ('52000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000003', 'COMPANY', 'Delta Servicios Profesionales SL', 'B44332211', 'B44332211', 'facturas@delta.local', NULL, 'Avenida Industrial 20', NULL, 'Valencia', 'Valencia', '46001', 'ES', 'ACTIVE');

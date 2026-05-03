CREATE TABLE fiscal_invoices (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    issuer_company_id VARCHAR(36) NOT NULL,
    customer_company_id VARCHAR(36) NOT NULL,
    invoice_number VARCHAR(80) NOT NULL,
    invoice_type VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    issue_date DATE NOT NULL,
    currency VARCHAR(3) NOT NULL,
    taxable_base DECIMAL(19, 2) NOT NULL,
    tax_total DECIMAL(19, 2) NOT NULL,
    total DECIMAL(19, 2) NOT NULL,
    rectifies_invoice_id VARCHAR(36),
    created_by_user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_fiscal_invoices PRIMARY KEY (id),
    CONSTRAINT fk_fiscal_invoices_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_fiscal_invoices_issuer FOREIGN KEY (issuer_company_id) REFERENCES companies (id),
    CONSTRAINT fk_fiscal_invoices_customer FOREIGN KEY (customer_company_id) REFERENCES companies (id),
    CONSTRAINT fk_fiscal_invoices_rectifies FOREIGN KEY (rectifies_invoice_id) REFERENCES fiscal_invoices (id),
    CONSTRAINT fk_fiscal_invoices_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_users (id),
    CONSTRAINT uk_fiscal_invoices_tenant_number UNIQUE (tenant_id, invoice_number)
);

CREATE TABLE fiscal_invoice_lines (
    id VARCHAR(36) NOT NULL,
    invoice_id VARCHAR(36) NOT NULL,
    line_number INT NOT NULL,
    description VARCHAR(500) NOT NULL,
    quantity DECIMAL(19, 4) NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL,
    tax_rate DECIMAL(5, 2) NOT NULL,
    line_base DECIMAL(19, 2) NOT NULL,
    tax_amount DECIMAL(19, 2) NOT NULL,
    line_total DECIMAL(19, 2) NOT NULL,
    CONSTRAINT pk_fiscal_invoice_lines PRIMARY KEY (id),
    CONSTRAINT fk_fiscal_invoice_lines_invoice FOREIGN KEY (invoice_id) REFERENCES fiscal_invoices (id),
    CONSTRAINT uk_fiscal_invoice_lines_number UNIQUE (invoice_id, line_number)
);

CREATE TABLE fiscal_invoice_taxes (
    id VARCHAR(36) NOT NULL,
    invoice_id VARCHAR(36) NOT NULL,
    tax_rate DECIMAL(5, 2) NOT NULL,
    taxable_base DECIMAL(19, 2) NOT NULL,
    tax_amount DECIMAL(19, 2) NOT NULL,
    CONSTRAINT pk_fiscal_invoice_taxes PRIMARY KEY (id),
    CONSTRAINT fk_fiscal_invoice_taxes_invoice FOREIGN KEY (invoice_id) REFERENCES fiscal_invoices (id),
    CONSTRAINT uk_fiscal_invoice_taxes_rate UNIQUE (invoice_id, tax_rate)
);

CREATE INDEX ix_fiscal_invoices_tenant_status ON fiscal_invoices (tenant_id, status, issue_date);
CREATE INDEX ix_fiscal_invoice_lines_invoice ON fiscal_invoice_lines (invoice_id, line_number);
CREATE INDEX ix_fiscal_invoice_taxes_invoice ON fiscal_invoice_taxes (invoice_id);

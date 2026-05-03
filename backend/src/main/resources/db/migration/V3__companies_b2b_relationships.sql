CREATE TABLE business_relationships (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    source_company_id VARCHAR(36) NOT NULL,
    target_company_id VARCHAR(36) NOT NULL,
    relationship_kind VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(500),
    starts_at DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_business_relationships PRIMARY KEY (id),
    CONSTRAINT fk_business_relationships_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_business_relationships_source FOREIGN KEY (source_company_id) REFERENCES companies (id),
    CONSTRAINT fk_business_relationships_target FOREIGN KEY (target_company_id) REFERENCES companies (id),
    CONSTRAINT uk_business_relationships_scope UNIQUE (tenant_id, source_company_id, target_company_id, relationship_kind)
);

CREATE INDEX ix_business_relationships_tenant_status ON business_relationships (tenant_id, status);
CREATE INDEX ix_business_relationships_source ON business_relationships (source_company_id);
CREATE INDEX ix_business_relationships_target ON business_relationships (target_company_id);

INSERT INTO business_relationships (
    id,
    tenant_id,
    source_company_id,
    target_company_id,
    relationship_kind,
    status,
    notes,
    starts_at
)
VALUES
    (
        '50000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000001',
        '40000000-0000-0000-0000-000000000001',
        '40000000-0000-0000-0000-000000000002',
        'CLIENT_MANAGEMENT',
        'ACTIVE',
        'Gestion fiscal y documental recurrente',
        DATE '2026-01-01'
    ),
    (
        '50000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000002',
        '40000000-0000-0000-0000-000000000003',
        '40000000-0000-0000-0000-000000000004',
        'SUPPLIER_PORTAL',
        'ACTIVE',
        'Intercambio documental con proveedor',
        DATE '2026-01-01'
    );

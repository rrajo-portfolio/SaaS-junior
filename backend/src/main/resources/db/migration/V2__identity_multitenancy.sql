CREATE TABLE tenants (
    id VARCHAR(36) NOT NULL,
    slug VARCHAR(80) NOT NULL,
    display_name VARCHAR(180) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_tenants PRIMARY KEY (id),
    CONSTRAINT uk_tenants_slug UNIQUE (slug)
);

CREATE TABLE app_users (
    id VARCHAR(36) NOT NULL,
    email VARCHAR(180) NOT NULL,
    display_name VARCHAR(180) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_app_users PRIMARY KEY (id),
    CONSTRAINT uk_app_users_email UNIQUE (email)
);

CREATE TABLE memberships (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(60) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_memberships PRIMARY KEY (id),
    CONSTRAINT fk_memberships_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_memberships_user FOREIGN KEY (user_id) REFERENCES app_users (id),
    CONSTRAINT uk_memberships_tenant_user UNIQUE (tenant_id, user_id)
);

CREATE TABLE companies (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    legal_name VARCHAR(220) NOT NULL,
    tax_id VARCHAR(40) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    relationship_type VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_companies PRIMARY KEY (id),
    CONSTRAINT fk_companies_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT uk_companies_tenant_tax_id UNIQUE (tenant_id, tax_id)
);

CREATE INDEX ix_memberships_user ON memberships (user_id);
CREATE INDEX ix_companies_tenant ON companies (tenant_id);

INSERT INTO tenants (id, slug, display_name, status)
VALUES
    ('10000000-0000-0000-0000-000000000001', 'norte-asesores', 'Norte Asesores', 'ACTIVE'),
    ('10000000-0000-0000-0000-000000000002', 'cobalto-industrial', 'Cobalto Industrial', 'ACTIVE');

INSERT INTO app_users (id, email, display_name, status)
VALUES
    ('20000000-0000-0000-0000-000000000001', 'ana.admin@fiscalsaas.local', 'Ana Admin', 'ACTIVE'),
    ('20000000-0000-0000-0000-000000000002', 'leo.accountant@fiscalsaas.local', 'Leo Accountant', 'ACTIVE'),
    ('20000000-0000-0000-0000-000000000003', 'maria.auditor@fiscalsaas.local', 'Maria Auditor', 'ACTIVE');

INSERT INTO memberships (id, tenant_id, user_id, role, status)
VALUES
    ('30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'platform_admin', 'ACTIVE'),
    ('30000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', 'platform_admin', 'ACTIVE'),
    ('30000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', 'accountant', 'ACTIVE'),
    ('30000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000003', 'auditor', 'ACTIVE');

INSERT INTO companies (id, tenant_id, legal_name, tax_id, country_code, relationship_type, status)
VALUES
    ('40000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'Norte Asesores SL', 'B12345678', 'ES', 'OWNER', 'ACTIVE'),
    ('40000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'Alba Retail Group SL', 'B87654321', 'ES', 'CLIENT', 'ACTIVE'),
    ('40000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002', 'Cobalto Industrial SA', 'A11223344', 'ES', 'OWNER', 'ACTIVE'),
    ('40000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000002', 'Delta Servicios Profesionales SL', 'B44332211', 'ES', 'SUPPLIER', 'ACTIVE');

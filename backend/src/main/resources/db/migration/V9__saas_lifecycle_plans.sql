CREATE TABLE subscription_plans (
    code VARCHAR(40) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    status VARCHAR(30) NOT NULL,
    monthly_price_cents INT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    max_users INT NOT NULL,
    max_documents INT NOT NULL,
    max_invoices INT NOT NULL,
    includes_verifactu BOOLEAN NOT NULL,
    includes_einvoice BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_subscription_plans PRIMARY KEY (code)
);

INSERT INTO subscription_plans (
    code,
    display_name,
    status,
    monthly_price_cents,
    currency,
    max_users,
    max_documents,
    max_invoices,
    includes_verifactu,
    includes_einvoice
)
VALUES
    ('starter', 'Starter', 'ACTIVE', 2900, 'EUR', 3, 250, 100, TRUE, FALSE),
    ('professional', 'Professional', 'ACTIVE', 7900, 'EUR', 12, 2500, 1000, TRUE, TRUE),
    ('business', 'Business', 'ACTIVE', 19900, 'EUR', 50, 15000, 5000, TRUE, TRUE);

ALTER TABLE tenants
    ADD COLUMN plan_code VARCHAR(40) NOT NULL DEFAULT 'starter';

ALTER TABLE tenants
    ADD COLUMN subscription_status VARCHAR(30) NOT NULL DEFAULT 'trialing';

ALTER TABLE tenants
    ADD COLUMN trial_ends_at TIMESTAMP(6) NULL;

ALTER TABLE tenants
    ADD COLUMN suspended_at TIMESTAMP(6) NULL;

ALTER TABLE tenants
    ADD CONSTRAINT fk_tenants_plan FOREIGN KEY (plan_code) REFERENCES subscription_plans (code);

CREATE TABLE tenant_lifecycle_events (
    id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(60) NOT NULL,
    actor_user_id VARCHAR(36) NOT NULL,
    from_status VARCHAR(30) NULL,
    to_status VARCHAR(30) NULL,
    from_plan_code VARCHAR(40) NULL,
    to_plan_code VARCHAR(40) NULL,
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_tenant_lifecycle_events PRIMARY KEY (id),
    CONSTRAINT fk_tenant_lifecycle_events_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_tenant_lifecycle_events_actor FOREIGN KEY (actor_user_id) REFERENCES app_users (id)
);

CREATE INDEX ix_tenant_lifecycle_events_tenant ON tenant_lifecycle_events (tenant_id, created_at);
CREATE INDEX ix_tenants_plan ON tenants (plan_code);
CREATE INDEX ix_tenants_subscription_status ON tenants (subscription_status);

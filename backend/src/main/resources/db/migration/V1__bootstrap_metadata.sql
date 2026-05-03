CREATE TABLE app_metadata (
    id CHAR(36) NOT NULL,
    metadata_key VARCHAR(120) NOT NULL,
    metadata_value VARCHAR(500) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_app_metadata PRIMARY KEY (id),
    CONSTRAINT uk_app_metadata_key UNIQUE (metadata_key)
);

INSERT INTO app_metadata (id, metadata_key, metadata_value)
VALUES ('00000000-0000-0000-0000-000000000001', 'schema.version', 'phase-1');

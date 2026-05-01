CREATE TABLE libraries
(
    id           VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    slug         TEXT        NOT NULL UNIQUE,
    name         TEXT        NOT NULL,
    description  TEXT,
    homepage_url TEXT,
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deleted_at   TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_libraries_status CHECK (status IN ('ACTIVE', 'PAUSED'))
);

CREATE INDEX ix_libraries_active ON libraries (slug) WHERE deleted_at IS NULL;

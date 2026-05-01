-- Doxen combined SQL schema
-- Target: PostgreSQL 16+
-- Vector storage/search: Qdrant only
-- PostgreSQL stores source-of-truth metadata, documents, chunks, API keys, and full-text search.

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ---------- Enums ----------

CREATE TYPE library_source_type AS ENUM (
    'LOCAL_FILE',
    'DOCS_SITE',
    'NPM_PACKAGE',
    'MAVEN_PACKAGE',
    'JSDOC',
    'JAVADOC',
    'ZIP_UPLOAD'
);

CREATE TYPE library_status AS ENUM (
    'ACTIVE',
    'PAUSED'
);

CREATE TYPE crawl_job_status AS ENUM (
    'QUEUED',
    'RUNNING',
    'SUCCEEDED',
    'FAILED',
    'CANCELLED'
);

CREATE TYPE source_document_status AS ENUM (
    'DISCOVERED',
    'FETCHED',
    'PARSED',
    'INDEXED',
    'FAILED'
);

CREATE TYPE api_key_status AS ENUM (
    'ACTIVE',
    'REVOKED'
);

CREATE TYPE api_key_scope AS ENUM (
    'search',
    'crawl',
    'admin',
    'mcp'
);

CREATE TYPE component_kind AS ENUM (
    'component',
    'function',
    'class',
    'hook',
    'directive',
    'guide',
    'module',
    'other'
);

CREATE TYPE embedding_distance_metric AS ENUM (
    'cosine',
    'dot',
    'euclid'
);

-- ---------- Shared updated_at trigger ----------

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ---------- Libraries ----------

CREATE TABLE libraries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    homepage_url TEXT,
    status library_status NOT NULL DEFAULT 'ACTIVE',
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_libraries_active ON libraries(slug) WHERE deleted_at IS NULL;

CREATE TRIGGER trg_libraries_updated_at
    BEFORE UPDATE ON libraries
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE library_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    library_id UUID NOT NULL REFERENCES libraries(id) ON DELETE CASCADE,
    version TEXT NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (library_id, version),
    UNIQUE (library_id, id)
);

CREATE UNIQUE INDEX ux_library_versions_one_default
    ON library_versions(library_id)
    WHERE is_default = true;

CREATE INDEX ix_library_versions_library_id
    ON library_versions(library_id);

CREATE TRIGGER trg_library_versions_updated_at
    BEFORE UPDATE ON library_versions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Embedding configuration used to create Qdrant vectors.
-- Keep one embedding space per Qdrant collection.
CREATE TABLE embedding_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL UNIQUE,
    provider TEXT NOT NULL, -- openai | cohere | ollama | etc.
    model TEXT NOT NULL,
    dimensions INTEGER NOT NULL CHECK (dimensions > 0),
    qdrant_collection_name TEXT NOT NULL UNIQUE,
    distance_metric embedding_distance_metric NOT NULL DEFAULT 'cosine',
    config JSONB NOT NULL DEFAULT '{}'::jsonb,
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_embedding_configs_provider_model ON embedding_configs(provider, model);
CREATE INDEX ix_embedding_configs_active ON embedding_configs(name) WHERE deleted_at IS NULL;

CREATE TRIGGER trg_embedding_configs_updated_at
    BEFORE UPDATE ON embedding_configs
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- A library version can have multiple documentation sources.
CREATE TABLE library_sources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    library_id UUID NOT NULL REFERENCES libraries(id) ON DELETE CASCADE,
    version_id UUID,
    embedding_config_id UUID REFERENCES embedding_configs(id) ON DELETE RESTRICT,
    source_type library_source_type NOT NULL,
    source_url TEXT,
    source_path TEXT,
    package_name TEXT,
    include_patterns TEXT[] NOT NULL DEFAULT '{}',
    exclude_patterns TEXT[] NOT NULL DEFAULT '{}',
    crawl_depth INTEGER NOT NULL DEFAULT 2 CHECK (crawl_depth >= 0),
    crawl_interval_mins INTEGER NOT NULL DEFAULT 1440 CHECK (crawl_interval_mins > 0),
    last_crawled_at TIMESTAMPTZ,
    config JSONB NOT NULL DEFAULT '{}'::jsonb,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (source_url IS NOT NULL OR source_path IS NOT NULL OR package_name IS NOT NULL),
    CONSTRAINT fk_library_sources_library_version
        FOREIGN KEY (library_id, version_id)
        REFERENCES library_versions(library_id, id)
        ON DELETE SET NULL (version_id)
);

CREATE INDEX ix_library_sources_library ON library_sources(library_id);
CREATE INDEX ix_library_sources_version ON library_sources(version_id);
CREATE INDEX ix_library_sources_embedding_config ON library_sources(embedding_config_id);
CREATE INDEX ix_library_sources_type ON library_sources(source_type);
CREATE INDEX ix_library_sources_enabled ON library_sources(enabled) WHERE enabled = true;
CREATE INDEX ix_library_sources_due ON library_sources(last_crawled_at) WHERE enabled = true;
CREATE UNIQUE INDEX ux_library_sources_url
    ON library_sources(library_id, version_id, source_url) NULLS NOT DISTINCT
    WHERE source_url IS NOT NULL;

CREATE TRIGGER trg_library_sources_updated_at
    BEFORE UPDATE ON library_sources
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------- API keys ----------

CREATE TABLE api_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    key_prefix TEXT NOT NULL CHECK (length(key_prefix) = 8),
    key_hash TEXT NOT NULL UNIQUE,
    status api_key_status NOT NULL DEFAULT 'ACTIVE',
    scopes api_key_scope[] NOT NULL DEFAULT ARRAY['search']::api_key_scope[],
    rate_limit_per_minute INTEGER NOT NULL DEFAULT 60 CHECK (rate_limit_per_minute > 0),
    expires_at TIMESTAMPTZ,
    last_used_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_api_keys_prefix ON api_keys(key_prefix);
CREATE INDEX ix_api_keys_active ON api_keys(status) WHERE status = 'ACTIVE';

CREATE TRIGGER trg_api_keys_updated_at
    BEFORE UPDATE ON api_keys
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------- Crawl jobs and source documents ----------

CREATE TABLE crawl_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    library_id UUID NOT NULL REFERENCES libraries(id) ON DELETE CASCADE,
    version_id UUID,
    source_id UUID REFERENCES library_sources(id) ON DELETE SET NULL,
    embedding_config_id UUID REFERENCES embedding_configs(id) ON DELETE RESTRICT,
    requested_by_api_key_id UUID REFERENCES api_keys(id) ON DELETE SET NULL,
    status crawl_job_status NOT NULL DEFAULT 'QUEUED',
    triggered_by TEXT, -- scheduler | api | user:<id>
    priority INTEGER NOT NULL DEFAULT 100, -- higher number = higher priority
    pages_discovered INTEGER NOT NULL DEFAULT 0 CHECK (pages_discovered >= 0),
    pages_fetched INTEGER NOT NULL DEFAULT 0 CHECK (pages_fetched >= 0),
    pages_failed INTEGER NOT NULL DEFAULT 0 CHECK (pages_failed >= 0),
    chunks_created INTEGER NOT NULL DEFAULT 0 CHECK (chunks_created >= 0),
    chunks_updated INTEGER NOT NULL DEFAULT 0 CHECK (chunks_updated >= 0),
    chunks_deleted INTEGER NOT NULL DEFAULT 0 CHECK (chunks_deleted >= 0),
    error_message TEXT,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (started_at IS NULL OR completed_at IS NULL OR started_at <= completed_at),
    CONSTRAINT fk_crawl_jobs_library_version
        FOREIGN KEY (library_id, version_id)
        REFERENCES library_versions(library_id, id)
        ON DELETE SET NULL (version_id)
);

CREATE INDEX ix_crawl_jobs_library ON crawl_jobs(library_id);
CREATE INDEX ix_crawl_jobs_version ON crawl_jobs(version_id);
CREATE INDEX ix_crawl_jobs_embedding_config ON crawl_jobs(embedding_config_id);
CREATE INDEX ix_crawl_jobs_dequeue ON crawl_jobs(status, priority DESC, created_at ASC)
    WHERE status = 'QUEUED';
CREATE INDEX ix_crawl_jobs_created_at ON crawl_jobs(created_at DESC);

CREATE TRIGGER trg_crawl_jobs_updated_at
    BEFORE UPDATE ON crawl_jobs
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- One row per fetched page/file/package document.
CREATE TABLE source_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    library_id UUID NOT NULL REFERENCES libraries(id) ON DELETE CASCADE,
    version_id UUID,
    source_id UUID REFERENCES library_sources(id) ON DELETE SET NULL,
    crawl_job_id UUID REFERENCES crawl_jobs(id) ON DELETE SET NULL,
    uri TEXT NOT NULL,
    title TEXT,
    content_type TEXT,
    content_hash TEXT,
    etag TEXT,
    last_modified TIMESTAMPTZ,
    status source_document_status NOT NULL DEFAULT 'DISCOVERED',
    deleted_at TIMESTAMPTZ,
    content_ref TEXT, -- optional object-storage/file reference for raw HTML/Markdown
    raw_content_preview TEXT, -- optional truncated debug preview; avoid storing full large pages inline
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    error_message TEXT,
    fetched_at TIMESTAMPTZ,
    indexed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE NULLS NOT DISTINCT (library_id, version_id, uri),
    CONSTRAINT fk_source_documents_library_version
        FOREIGN KEY (library_id, version_id)
        REFERENCES library_versions(library_id, id)
        ON DELETE SET NULL (version_id)
);

CREATE INDEX ix_source_documents_library_version ON source_documents(library_id, version_id);
CREATE INDEX ix_source_documents_live ON source_documents(library_id, version_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_source_documents_source ON source_documents(source_id);
CREATE INDEX ix_source_documents_hash ON source_documents(content_hash);
CREATE INDEX ix_source_documents_status ON source_documents(status);
CREATE INDEX ix_source_documents_uri_trgm ON source_documents USING GIN (uri gin_trgm_ops);

CREATE TRIGGER trg_source_documents_updated_at
    BEFORE UPDATE ON source_documents
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------- Components and chunks ----------

CREATE TABLE components (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    library_id UUID NOT NULL REFERENCES libraries(id) ON DELETE CASCADE,
    version_id UUID,
    name TEXT NOT NULL,
    slug TEXT NOT NULL,
    kind component_kind,
    description TEXT,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE NULLS NOT DISTINCT (library_id, version_id, slug),
    CONSTRAINT fk_components_library_version
        FOREIGN KEY (library_id, version_id)
        REFERENCES library_versions(library_id, id)
        ON DELETE SET NULL (version_id)
);

CREATE INDEX ix_components_library_version ON components(library_id, version_id);
CREATE INDEX ix_components_name ON components(name);
CREATE INDEX ix_components_slug_trgm ON components USING GIN (slug gin_trgm_ops);
CREATE INDEX ix_components_name_trgm ON components USING GIN (name gin_trgm_ops);

CREATE TRIGGER trg_components_updated_at
    BEFORE UPDATE ON components
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE doc_chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    library_id UUID NOT NULL REFERENCES libraries(id) ON DELETE CASCADE,
    version_id UUID,
    source_document_id UUID NOT NULL REFERENCES source_documents(id) ON DELETE CASCADE,
    component_id UUID REFERENCES components(id) ON DELETE SET NULL,
    component_name TEXT,
    component_slug TEXT,
    ordinal INTEGER NOT NULL CHECK (ordinal >= 0),
    heading_path TEXT[] NOT NULL DEFAULT '{}',
    heading TEXT,
    content TEXT NOT NULL,
    content_hash TEXT NOT NULL,
    token_count INTEGER NOT NULL DEFAULT 0 CHECK (token_count >= 0),
    qdrant_point_id UUID,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    search_vector TSVECTOR GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(component_name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(array_to_string(heading_path, ' '), '')), 'A') ||
        setweight(to_tsvector('english', coalesce(heading, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(content, '')), 'B')
    ) STORED,
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (source_document_id, ordinal),
    CONSTRAINT fk_doc_chunks_library_version
        FOREIGN KEY (library_id, version_id)
        REFERENCES library_versions(library_id, id)
        ON DELETE SET NULL (version_id)
);

CREATE INDEX ix_doc_chunks_library_version ON doc_chunks(library_id, version_id);
CREATE INDEX ix_doc_chunks_live ON doc_chunks(library_id, version_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_doc_chunks_component ON doc_chunks(component_id);
CREATE INDEX ix_doc_chunks_component_slug ON doc_chunks(component_slug);
CREATE INDEX ix_doc_chunks_document ON doc_chunks(source_document_id);
CREATE INDEX ix_doc_chunks_hash ON doc_chunks(content_hash);
CREATE INDEX ix_doc_chunks_search_vector ON doc_chunks USING GIN(search_vector);
CREATE INDEX ix_doc_chunks_heading_trgm ON doc_chunks USING GIN (heading gin_trgm_ops);

CREATE UNIQUE INDEX ux_doc_chunks_qdrant_point
    ON doc_chunks(qdrant_point_id)
    WHERE qdrant_point_id IS NOT NULL;

CREATE TRIGGER trg_doc_chunks_updated_at
    BEFORE UPDATE ON doc_chunks
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------- Usage and analytics ----------

CREATE TABLE api_key_usage_daily (
    api_key_id UUID NOT NULL REFERENCES api_keys(id) ON DELETE CASCADE,
    usage_date DATE NOT NULL,
    search_requests INTEGER NOT NULL DEFAULT 0 CHECK (search_requests >= 0),
    crawl_requests INTEGER NOT NULL DEFAULT 0 CHECK (crawl_requests >= 0),
    mcp_requests INTEGER NOT NULL DEFAULT 0 CHECK (mcp_requests >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (api_key_id, usage_date)
);

CREATE INDEX ix_api_key_usage_daily_usage_date ON api_key_usage_daily(usage_date);

CREATE TRIGGER trg_api_key_usage_daily_updated_at
    BEFORE UPDATE ON api_key_usage_daily
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE search_queries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    api_key_id UUID REFERENCES api_keys(id) ON DELETE SET NULL,
    query TEXT NOT NULL,
    library_id UUID REFERENCES libraries(id) ON DELETE SET NULL,
    version_id UUID,
    component_id UUID REFERENCES components(id) ON DELETE SET NULL,
    result_count INTEGER NOT NULL DEFAULT 0 CHECK (result_count >= 0),
    latency_ms INTEGER CHECK (latency_ms IS NULL OR latency_ms >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_search_queries_library_version
        FOREIGN KEY (library_id, version_id)
        REFERENCES library_versions(library_id, id)
        ON DELETE SET NULL (version_id)
);

CREATE INDEX ix_search_queries_created ON search_queries(created_at DESC);
CREATE INDEX ix_search_queries_library_version ON search_queries(library_id, version_id);
CREATE INDEX ix_search_queries_api_key ON search_queries(api_key_id);
CREATE INDEX ix_search_queries_query_trgm ON search_queries USING GIN (query gin_trgm_ops);
CREATE TABLE chunk_feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    search_query_id UUID REFERENCES search_queries(id) ON DELETE CASCADE,
    chunk_id UUID NOT NULL REFERENCES doc_chunks(id) ON DELETE CASCADE,
    api_key_id UUID REFERENCES api_keys(id) ON DELETE SET NULL,
    rating INTEGER CHECK (rating IN (-1, 1)), -- nullable to allow comment-only feedback
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE NULLS NOT DISTINCT (search_query_id, chunk_id, api_key_id),
    CHECK (rating IS NOT NULL OR comment IS NOT NULL)
);

CREATE INDEX ix_chunk_feedback_chunk ON chunk_feedback(chunk_id);
CREATE INDEX ix_chunk_feedback_api_key ON chunk_feedback(api_key_id);

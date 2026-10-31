CREATE TABLE documents (
    id           UUID PRIMARY KEY,
    filename     VARCHAR(255) NOT NULL,
    content_type VARCHAR(127),
    size_bytes   BIGINT       NOT NULL,
    chunk_count  INTEGER      NOT NULL DEFAULT 0,
    status       VARCHAR(20)  NOT NULL,
    error        TEXT,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_documents_created_at ON documents (created_at DESC);

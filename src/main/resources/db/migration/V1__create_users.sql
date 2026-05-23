CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT uuidv7(),
    email       TEXT NOT NULL,
    name        TEXT,
    provider    TEXT NOT NULL,
    provider_id TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (provider, provider_id)
);

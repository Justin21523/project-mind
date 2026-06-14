-- ============================================================
-- ProjectMind API - Initial schema (Phase 1)
-- Modules: user, auth (refresh tokens), workspace, tag
-- ============================================================

-- ---------- users ----------
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    full_name   VARCHAR(150),
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    created_by  VARCHAR(50),
    updated_by  VARCHAR(50),
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_users_email ON users (email);

-- ---------- user_roles (element collection) ----------
CREATE TABLE user_roles (
    user_id BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role    VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- ---------- refresh_tokens ----------
CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);

-- ---------- workspaces ----------
CREATE TABLE workspaces (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    description VARCHAR(2000),
    owner_id    BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    created_by  VARCHAR(50),
    updated_by  VARCHAR(50),
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_workspaces_owner_id ON workspaces (owner_id);
CREATE UNIQUE INDEX uq_workspaces_owner_name ON workspaces (owner_id, name) WHERE deleted = FALSE;

-- ---------- tags ----------
CREATE TABLE tags (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    color      VARCHAR(20),
    owner_id   BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    deleted    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version    BIGINT      NOT NULL DEFAULT 0
);

CREATE INDEX idx_tags_owner_id ON tags (owner_id);
CREATE UNIQUE INDEX uq_tags_owner_name ON tags (owner_id, name) WHERE deleted = FALSE;

-- ============================================================
-- ProjectMind API - Phase 2 schema
-- Modules: project, task, note, prompt, resource, modelregistry, audit
-- ============================================================

-- ---------- projects ----------
CREATE TABLE projects (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    description    VARCHAR(4000),
    status         VARCHAR(30)  NOT NULL,
    priority       VARCHAR(20)  NOT NULL,
    repository_url VARCHAR(500),
    start_date     DATE,
    target_date    DATE,
    progress       INT          NOT NULL DEFAULT 0,
    workspace_id   BIGINT       NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    owner_id       BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,
    created_by     VARCHAR(50),
    updated_by     VARCHAR(50),
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_projects_owner_id ON projects (owner_id);
CREATE INDEX idx_projects_workspace_id ON projects (workspace_id);
CREATE INDEX idx_projects_status ON projects (status);
CREATE UNIQUE INDEX uq_projects_workspace_name ON projects (workspace_id, name) WHERE deleted = FALSE;

CREATE TABLE project_tech_stack (
    project_id BIGINT      NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    tech       VARCHAR(60) NOT NULL,
    PRIMARY KEY (project_id, tech)
);

CREATE TABLE project_tags (
    project_id BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    tag_id     BIGINT NOT NULL REFERENCES tags (id) ON DELETE CASCADE,
    PRIMARY KEY (project_id, tag_id)
);

-- ---------- tasks ----------
CREATE TABLE tasks (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    description  VARCHAR(4000),
    status       VARCHAR(30)  NOT NULL,
    priority     VARCHAR(20)  NOT NULL,
    due_date     DATE,
    project_id   BIGINT       REFERENCES projects (id) ON DELETE SET NULL,
    workspace_id BIGINT       NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    owner_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    created_by   VARCHAR(50),
    updated_by   VARCHAR(50),
    version      BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_tasks_owner_id ON tasks (owner_id);
CREATE INDEX idx_tasks_workspace_id ON tasks (workspace_id);
CREATE INDEX idx_tasks_project_id ON tasks (project_id);
CREATE INDEX idx_tasks_status ON tasks (status);

-- ---------- notes ----------
CREATE TABLE notes (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    content      TEXT,
    type         VARCHAR(30)  NOT NULL,
    workspace_id BIGINT       NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    owner_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    created_by   VARCHAR(50),
    updated_by   VARCHAR(50),
    version      BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_notes_owner_id ON notes (owner_id);
CREATE INDEX idx_notes_workspace_id ON notes (workspace_id);
CREATE INDEX idx_notes_type ON notes (type);

CREATE TABLE note_tags (
    note_id BIGINT NOT NULL REFERENCES notes (id) ON DELETE CASCADE,
    tag_id  BIGINT NOT NULL REFERENCES tags (id) ON DELETE CASCADE,
    PRIMARY KEY (note_id, tag_id)
);

-- ---------- prompts ----------
CREATE TABLE prompts (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(200) NOT NULL,
    content       TEXT         NOT NULL,
    target_model  VARCHAR(100),
    task_type     VARCHAR(100),
    rating        INT,
    prompt_version INT         NOT NULL DEFAULT 1,
    notes         VARCHAR(2000),
    workspace_id  BIGINT       NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    owner_id      BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    created_by    VARCHAR(50),
    updated_by    VARCHAR(50),
    version       BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_prompts_owner_id ON prompts (owner_id);
CREATE INDEX idx_prompts_workspace_id ON prompts (workspace_id);

CREATE TABLE prompt_tags (
    prompt_id BIGINT NOT NULL REFERENCES prompts (id) ON DELETE CASCADE,
    tag_id    BIGINT NOT NULL REFERENCES tags (id) ON DELETE CASCADE,
    PRIMARY KEY (prompt_id, tag_id)
);

-- ---------- resources ----------
CREATE TABLE resources (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    url          VARCHAR(1000) NOT NULL,
    type         VARCHAR(30)  NOT NULL,
    description  VARCHAR(2000),
    workspace_id BIGINT       NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    owner_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    created_by   VARCHAR(50),
    updated_by   VARCHAR(50),
    version      BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_resources_owner_id ON resources (owner_id);
CREATE INDEX idx_resources_workspace_id ON resources (workspace_id);
CREATE INDEX idx_resources_type ON resources (type);

CREATE TABLE resource_tags (
    resource_id BIGINT NOT NULL REFERENCES resources (id) ON DELETE CASCADE,
    tag_id      BIGINT NOT NULL REFERENCES tags (id) ON DELETE CASCADE,
    PRIMARY KEY (resource_id, tag_id)
);

-- ---------- ai_models (model registry) ----------
CREATE TABLE ai_models (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(150) NOT NULL,
    provider          VARCHAR(100),
    modality          VARCHAR(30)  NOT NULL,
    format            VARCHAR(30),
    quantization      VARCHAR(50),
    estimated_vram_mb INT,
    use_case          VARCHAR(500),
    notes             VARCHAR(2000),
    owner_id          BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    created_by        VARCHAR(50),
    updated_by        VARCHAR(50),
    version           BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_ai_models_owner_id ON ai_models (owner_id);
CREATE UNIQUE INDEX uq_ai_models_owner_name ON ai_models (owner_id, name) WHERE deleted = FALSE;

-- ---------- audit_logs ----------
CREATE TABLE audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    action      VARCHAR(30) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id   BIGINT,
    details     VARCHAR(500),
    created_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id);

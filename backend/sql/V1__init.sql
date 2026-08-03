-- ============================================================================
-- Email Delivery Platform - Initial schema (PostgreSQL)
-- ============================================================================
-- Applies cleanly on an empty database. The application also runs with
-- Hibernate ddl-auto=validate in production, so keep this file in sync with
-- the JPA entities under src/main/java/com/emailplatform/model.
-- ============================================================================

CREATE TABLE IF NOT EXISTS users (
    id          VARCHAR(36)  PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    role        VARCHAR(32)  NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email ON users (LOWER(email));

CREATE TABLE IF NOT EXISTS campaigns (
    id            VARCHAR(36) PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    template_id   VARCHAR(255),
    user_id       VARCHAR(255),
    contact_list  TEXT,
    status        VARCHAR(32)  NOT NULL,
    sent_count    INTEGER,
    opened_count  INTEGER,
    clicked_count INTEGER,
    scheduled_at  TIMESTAMP,
    sent_at       TIMESTAMP,
    created_at    TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_campaigns_user ON campaigns (user_id);

CREATE TABLE IF NOT EXISTS contacts (
    id         VARCHAR(36) PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    list       VARCHAR(255),
    metadata   TEXT,
    user_id    VARCHAR(255),
    created_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_contacts_user ON contacts (user_id);
CREATE INDEX IF NOT EXISTS idx_contacts_list ON contacts (list);

CREATE TABLE IF NOT EXISTS templates (
    id         VARCHAR(36)  PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    subject    TEXT         NOT NULL,
    body       TEXT         NOT NULL,
    user_id    VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_templates_user ON templates (user_id);

CREATE TABLE IF NOT EXISTS api_keys (
    id         VARCHAR(36)  PRIMARY KEY,
    key_value  VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    user_id    VARCHAR(255),
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_api_keys_user ON api_keys (user_id);

CREATE TABLE IF NOT EXISTS analytics_events (
    id          VARCHAR(36)  PRIMARY KEY,
    campaign_id VARCHAR(255) NOT NULL,
    contact_id  VARCHAR(255) NOT NULL,
    event_type  VARCHAR(64)  NOT NULL,
    url         TEXT,
    timestamp   TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_analytics_campaign ON analytics_events (campaign_id);
CREATE INDEX IF NOT EXISTS idx_analytics_event_type ON analytics_events (event_type);

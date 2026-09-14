-- Initial schema for the Adaptive Flashcard Scheduler
-- Managed by Flyway; Hibernate ddl-auto is set to "validate" so this file is the
-- single source of truth for the database structure.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username         VARCHAR(50)  NOT NULL UNIQUE,
    email            VARCHAR(255) NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    display_name     VARCHAR(100),
    role             VARCHAR(20)  NOT NULL DEFAULT 'USER',
    current_streak   INTEGER      NOT NULL DEFAULT 0,
    longest_streak   INTEGER      NOT NULL DEFAULT 0,
    last_study_date  DATE,
    enabled          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE decks (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name         VARCHAR(120) NOT NULL,
    description  VARCHAR(500),
    subject      VARCHAR(30),
    is_archived  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_deck_owner ON decks(owner_id);

CREATE TABLE tags (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name      VARCHAR(40) NOT NULL,
    UNIQUE (owner_id, name)
);

CREATE TABLE cards (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deck_id          UUID NOT NULL REFERENCES decks(id) ON DELETE CASCADE,
    front            TEXT NOT NULL,
    back             TEXT NOT NULL,
    hint             VARCHAR(500),
    ease_factor      DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    repetitions      INTEGER NOT NULL DEFAULT 0,
    interval_days    INTEGER NOT NULL DEFAULT 0,
    due_date         DATE,
    last_reviewed_at TIMESTAMPTZ,
    total_reviews    INTEGER NOT NULL DEFAULT 0,
    lapses           INTEGER NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_card_deck ON cards(deck_id);
CREATE INDEX idx_card_due_date ON cards(due_date);

CREATE TABLE card_tags (
    card_id UUID NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
    tag_id  UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (card_id, tag_id)
);

CREATE TABLE review_logs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id             UUID NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    quality             INTEGER NOT NULL,
    ease_factor_before  DOUBLE PRECISION NOT NULL,
    ease_factor_after   DOUBLE PRECISION NOT NULL,
    interval_before     INTEGER NOT NULL,
    interval_after      INTEGER NOT NULL,
    repetitions_before  INTEGER NOT NULL,
    repetitions_after   INTEGER NOT NULL,
    response_time_ms    BIGINT,
    reviewed_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_review_card ON review_logs(card_id);
CREATE INDEX idx_review_user ON review_logs(user_id);
CREATE INDEX idx_review_reviewed_at ON review_logs(reviewed_at);

CREATE TABLE idempotency_records
(
    id              UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    user_id         VARCHAR(255)             NOT NULL,
    idempotency_key VARCHAR(255)             NOT NULL,
    endpoint        VARCHAR(255)             NOT NULL,
    http_status     INT                      NOT NULL,
    response_body   TEXT                     NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_idempotency_key UNIQUE (user_id, idempotency_key)
);

CREATE INDEX idx_idempotency_created_at ON idempotency_records (created_at);
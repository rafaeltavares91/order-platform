CREATE TABLE outbox_messages (
    public_id uuid PRIMARY KEY,
    aggregate_id uuid NOT NULL,
    event_type varchar(100) NOT NULL,
    payload jsonb NOT NULL,
    status varchar(20) NOT NULL,
    attempt_count integer NOT NULL DEFAULT 0,
    next_attempt_at timestamptz NOT NULL,
    locked_until timestamptz,
    published_at timestamptz,
    last_error varchar(1000),
    created_at timestamptz NOT NULL,
    CONSTRAINT chk_outbox_status CHECK (status IN ('PENDING', 'PROCESSING', 'PUBLISHED')),
    CONSTRAINT chk_outbox_attempt_count CHECK (attempt_count >= 0)
);

CREATE INDEX idx_outbox_pending
    ON outbox_messages (next_attempt_at, created_at)
    WHERE status <> 'PUBLISHED';

CREATE TABLE inbox_messages (
    event_id uuid PRIMARY KEY,
    event_type varchar(100) NOT NULL,
    received_at timestamptz NOT NULL
);

ALTER TABLE orders
    ADD COLUMN payment_id varchar(200),
    ADD COLUMN paid_at timestamptz,
    ADD CONSTRAINT uq_orders_payment_id UNIQUE (payment_id),
    ADD CONSTRAINT chk_orders_payment_fields CHECK (
        (payment_id IS NULL AND paid_at IS NULL)
        OR (payment_id IS NOT NULL AND btrim(payment_id) <> '' AND paid_at IS NOT NULL)
    );

ALTER TABLE customer_orders
    DROP CONSTRAINT chk_customer_orders_status,
    DROP CONSTRAINT chk_customer_orders_total;

ALTER TABLE customer_orders
    RENAME COLUMN total TO amount;

ALTER TABLE customer_orders
    ALTER COLUMN currency TYPE varchar(3),
    ADD COLUMN credit_date date;

UPDATE customer_orders
SET status = 'CREATED',
    credit_date = (created_at AT TIME ZONE 'UTC')::date
WHERE status = 'PENDING';

ALTER TABLE customer_orders
    ALTER COLUMN credit_date SET NOT NULL,
    ADD CONSTRAINT chk_customer_orders_amount CHECK (amount > 0),
    ADD CONSTRAINT chk_customer_orders_status CHECK (
        status IN ('CREATED', 'WAITING_PAYMENT', 'PAID', 'CREDITED', 'CANCELED')
    ),
    ADD CONSTRAINT chk_customer_orders_credit_date
        CHECK (credit_date >= (created_at AT TIME ZONE 'UTC')::date);

DROP TABLE order_lines;

CREATE TABLE customers (
    id uuid PRIMARY KEY,
    document varchar(50) NOT NULL,
    name varchar(200) NOT NULL,
    balance numeric(19, 4) NOT NULL,
    balance_currency varchar(3) NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_customers_document UNIQUE (document),
    CONSTRAINT chk_customers_document_not_blank CHECK (btrim(document) <> ''),
    CONSTRAINT chk_customers_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT chk_customers_balance CHECK (balance >= 0),
    CONSTRAINT chk_customers_balance_currency CHECK (balance_currency ~ '^[A-Z]{3}$'),
    CONSTRAINT chk_customers_timestamps CHECK (updated_at >= created_at),
    CONSTRAINT chk_customers_version CHECK (version >= 0)
);

CREATE TABLE orders (
    id uuid PRIMARY KEY,
    status varchar(30) NOT NULL,
    credit_date date NOT NULL,
    total_amount numeric(19, 4) NOT NULL,
    currency varchar(3) NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT chk_orders_status CHECK (
        status IN ('CREATED', 'WAITING_PAYMENT', 'PAID', 'CREDITED', 'CANCELED')
    ),
    CONSTRAINT chk_orders_total_amount CHECK (total_amount > 0),
    CONSTRAINT chk_orders_currency CHECK (currency ~ '^[A-Z]{3}$'),
    CONSTRAINT chk_orders_credit_date CHECK (
        credit_date >= (created_at AT TIME ZONE 'UTC')::date
    ),
    CONSTRAINT chk_orders_timestamps CHECK (updated_at >= created_at),
    CONSTRAINT chk_orders_version CHECK (version >= 0)
);

CREATE TABLE order_items (
    id uuid PRIMARY KEY,
    order_id uuid NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    item_index integer NOT NULL,
    customer_id uuid NOT NULL REFERENCES customers(id),
    amount numeric(19, 4) NOT NULL,
    currency varchar(3) NOT NULL,
    status varchar(30) NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_order_items_order_index UNIQUE (order_id, item_index),
    CONSTRAINT chk_order_items_index CHECK (item_index >= 0),
    CONSTRAINT chk_order_items_amount CHECK (amount > 0),
    CONSTRAINT chk_order_items_currency CHECK (currency ~ '^[A-Z]{3}$'),
    CONSTRAINT chk_order_items_status CHECK (status IN ('PENDING', 'CREDITED', 'CANCELED')),
    CONSTRAINT chk_order_items_timestamps CHECK (updated_at >= created_at),
    CONSTRAINT chk_order_items_version CHECK (version >= 0)
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_customer_id ON order_items(customer_id);

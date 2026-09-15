CREATE TABLE customer_orders (
    id uuid PRIMARY KEY,
    customer_id varchar(100) NOT NULL,
    status varchar(30) NOT NULL,
    currency char(3) NOT NULL,
    total numeric(19, 4) NOT NULL,
    created_at timestamptz NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT chk_customer_orders_status CHECK (status IN ('PENDING')),
    CONSTRAINT chk_customer_orders_currency CHECK (currency ~ '^[A-Z]{3}$'),
    CONSTRAINT chk_customer_orders_total CHECK (total >= 0),
    CONSTRAINT chk_customer_orders_version CHECK (version >= 0)
);

CREATE TABLE order_lines (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id uuid NOT NULL REFERENCES customer_orders(id) ON DELETE CASCADE,
    line_number integer NOT NULL,
    sku varchar(100) NOT NULL,
    quantity integer NOT NULL,
    unit_price numeric(19, 4) NOT NULL,
    CONSTRAINT uq_order_lines_order_line_number UNIQUE (order_id, line_number),
    CONSTRAINT chk_order_lines_line_number CHECK (line_number > 0),
    CONSTRAINT chk_order_lines_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_lines_unit_price CHECK (unit_price > 0)
);

CREATE INDEX idx_customer_orders_customer_id ON customer_orders(customer_id);
CREATE INDEX idx_customer_orders_created_at ON customer_orders(created_at);

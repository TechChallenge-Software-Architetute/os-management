CREATE SEQUENCE IF NOT EXISTS budget_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS budget_item_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT PRIMARY KEY DEFAULT nextval('budget_seq'),
    service_order_id UUID NOT NULL UNIQUE,
    total_price NUMERIC(19,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS budget_items (
    id BIGINT PRIMARY KEY DEFAULT nextval('budget_item_seq'),
    budget_id BIGINT NOT NULL REFERENCES budgets(id),
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(255) NOT NULL,
    product_type VARCHAR(50) NOT NULL,
    quantity NUMERIC(19,2) NOT NULL,
    unit_price NUMERIC(19,2) NOT NULL,
    total_price NUMERIC(19,2) NOT NULL
);

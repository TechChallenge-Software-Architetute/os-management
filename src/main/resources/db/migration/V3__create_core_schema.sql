-- Converted from the former PostgreSQL init scripts.
-- Database creation/destruction is intentionally outside Flyway.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS groups (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_groups (
    user_id UUID NOT NULL,
    group_id UUID NOT NULL,

    PRIMARY KEY (user_id, group_id),

    CONSTRAINT fk_user_groups_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_groups_group
        FOREIGN KEY (group_id) REFERENCES groups(id)
        ON DELETE CASCADE
);

CREATE SEQUENCE IF NOT EXISTS clients_seq
    START WITH 1
    INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS clients (
    id BIGINT PRIMARY KEY DEFAULT nextval('clients_seq'),
    name VARCHAR(255) NOT NULL,
    document VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(255),
    phone VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE clients ADD COLUMN IF NOT EXISTS document VARCHAR(14);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS cpf VARCHAR(11);
UPDATE clients SET document = cpf WHERE document IS NULL AND cpf IS NOT NULL;
UPDATE clients SET cpf = document WHERE cpf IS NULL AND document IS NOT NULL;

CREATE SEQUENCE IF NOT EXISTS vehicles_seq
    START WITH 1
    INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT PRIMARY KEY DEFAULT nextval('vehicles_seq'),
    client_id BIGINT NOT NULL,
    plate VARCHAR(7) NOT NULL UNIQUE,
    brand VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    year INTEGER NOT NULL,
    color VARCHAR(255),
    type VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vehicles_clients
        FOREIGN KEY (client_id)
        REFERENCES clients (id)
);

CREATE TABLE IF NOT EXISTS service_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS service (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_type_name VARCHAR(255) NOT NULL,
    id_os UUID NOT NULL,
    service_status TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS service_order (
    id UUID PRIMARY KEY,
    service_type_name VARCHAR(255) NOT NULL,
    service_status VARCHAR(30) DEFAULT 'RECEBIDA',
    list_service VARCHAR(255) NOT NULL,
    cpf_cnpj VARCHAR(50) NOT NULL,
    placa VARCHAR(20) NOT NULL,
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE SEQUENCE IF NOT EXISTS product_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY DEFAULT nextval('product_seq'),
    product_type VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(255) NOT NULL UNIQUE,
    unit VARCHAR(50) NOT NULL,
    category VARCHAR(255),
    brand VARCHAR(255),
    cost_price NUMERIC(19,2) NOT NULL,
    sale_price NUMERIC(19,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS parts (
    id BIGINT PRIMARY KEY REFERENCES products(id),
    manufacturer_code VARCHAR(255),
    warranty_months INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS supplies (
    id BIGINT PRIMARY KEY REFERENCES products(id),
    fractional_allowed BOOLEAN,
    package_size NUMERIC(19,2)
);

CREATE SEQUENCE IF NOT EXISTS stock_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS stocks (
    id BIGINT PRIMARY KEY DEFAULT nextval('stock_seq'),
    product_id BIGINT NOT NULL UNIQUE,
    quantity NUMERIC(19,2) NOT NULL DEFAULT 0,
    reserved_quantity NUMERIC(19,2) NOT NULL DEFAULT 0,
    minimum_quantity NUMERIC(19,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE SEQUENCE IF NOT EXISTS stock_movement_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS stock_movements (
    id BIGINT PRIMARY KEY DEFAULT nextval('stock_movement_seq'),
    stock_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    quantity NUMERIC(19,2) NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE SEQUENCE IF NOT EXISTS stock_reservation_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS stock_reservations (
    id BIGINT PRIMARY KEY DEFAULT nextval('stock_reservation_seq'),
    stock_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    service_order_id UUID NOT NULL,
    quantity NUMERIC(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

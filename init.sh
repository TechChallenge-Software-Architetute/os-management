#!/bin/bash
set -e

echo "=========================================="
echo "Iniciando script de inicialização do PostgreSQL"
echo "=========================================="

echo "=========================================="
echo "          TABLE USERS AND ROLES           "
echo "=========================================="
echo "✓ Criando tabelas de usuários, roles e grupos..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

    CREATE TABLE users (
        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
        email VARCHAR(255) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL
    );

    CREATE TABLE roles (
        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
        name VARCHAR(50) NOT NULL UNIQUE
    );

    CREATE TABLE groups (
        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
        name VARCHAR(100) NOT NULL UNIQUE
    );

    CREATE TABLE user_roles (
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

    CREATE TABLE user_groups (
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
EOSQL

echo "✓ Inserindo users..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-'EOSQL'
    INSERT INTO roles (id, name) VALUES
    (uuid_generate_v4(), 'ROLE_ADMIN'),
    (uuid_generate_v4(), 'ROLE_USER'),
    (uuid_generate_v4(), 'ROLE_TECHNICIAN');

    INSERT INTO users (id, email, password)
    VALUES (
        uuid_generate_v4(),
        'superadmin@system.com',
        '$2a$12$RJVIgDQpKX6.CtZiY9BQB.RNqNiDU7Y0Y6AMMlLUrxyApokRvMVrC' --coxinha123
    );

    INSERT INTO user_roles (user_id, role_id)
    SELECT u.id, r.id
    FROM users u, roles r
    WHERE u.email = 'superadmin@system.com';
EOSQL

echo "✓ Verificando dados inseridos em Users..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT COUNT(*) as total_users FROM users;
    SELECT id, email, password FROM users ORDER BY email;
EOSQL


echo "=========================================="
echo "              TABLE CLIENTS               "
echo "=========================================="
echo "✓ Criando sequence 'clients_seq'..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE SEQUENCE IF NOT EXISTS clients_seq
        START WITH 1
        INCREMENT BY 50;
EOSQL

echo "✓ Criando tabela 'clients'..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
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
EOSQL

echo "✓ Inserindo dados mock na tabela 'clients'..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    INSERT INTO clients (id, name, document, email, phone, active, created_at, updated_at)
    SELECT 1, 'JOAO DA SILVA', '52998224725', 'joao.silva@email.com', '(11) 99999-1234', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM clients WHERE document = '52998224725');

    INSERT INTO clients (id, name, document, email, phone, active, created_at, updated_at)
    SELECT 2, 'MARIA SOUZA', '07124632080', 'maria.souza@email.com', '(21) 98888-5678', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM clients WHERE document = '07124632080');

    INSERT INTO clients (id, name, document, email, phone, active, created_at, updated_at)
    SELECT 3, 'CARLOS OLIVEIRA', '18746880011', 'carlos.oliveira@email.com', '(31) 97777-9012', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM clients WHERE document = '18746880011');

    SELECT setval('clients_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM clients), 1));
EOSQL

echo "✓ Verificando tabela clients..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT COUNT(*) as total_clients FROM clients;
    SELECT id, name, document, email, phone, active FROM clients ORDER BY id;
EOSQL


echo "=========================================="
echo "              TABLE VEHICLES              "
echo "=========================================="
echo "✓ Criando sequence 'vehicles_seq'..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE SEQUENCE IF NOT EXISTS vehicles_seq
        START WITH 1
        INCREMENT BY 50;
EOSQL

echo "✓ Criando tabela 'vehicles'..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
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
EOSQL

echo "✓ Inserindo dados mock na tabela 'vehicles'..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    INSERT INTO vehicles (id, client_id, plate, brand, model, year, color, type, active, created_at, updated_at)
    SELECT 1, c.id, 'ABC1234', 'TOYOTA', 'COROLLA', 2020, 'PRATA', 'CAR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM clients c
    WHERE c.document = '52998224725'
      AND NOT EXISTS (SELECT 1 FROM vehicles WHERE plate = 'ABC1234');

    INSERT INTO vehicles (id, client_id, plate, brand, model, year, color, type, active, created_at, updated_at)
    SELECT 2, c.id, 'XYZ1A23', 'HONDA', 'CIVIC', 2021, 'PRETO', 'CAR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM clients c
    WHERE c.document = '52998224725'
      AND NOT EXISTS (SELECT 1 FROM vehicles WHERE plate = 'XYZ1A23');

    INSERT INTO vehicles (id, client_id, plate, brand, model, year, color, type, active, created_at, updated_at)
    SELECT 3, c.id, 'DEF5678', 'VOLKSWAGEN', 'GOL', 2019, 'BRANCO', 'CAR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM clients c
    WHERE c.document = '07124632080'
      AND NOT EXISTS (SELECT 1 FROM vehicles WHERE plate = 'DEF5678');

    SELECT setval('vehicles_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM vehicles), 1));
EOSQL

echo "✓ Verificando tabela vehicles..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT COUNT(*) as total_vehicles FROM vehicles;
    SELECT id, client_id, plate, brand, model, year, color, type, active FROM vehicles ORDER BY id;
EOSQL


echo "=========================================="
echo "           TABLE SERVICE_TYPE             "
echo "=========================================="
echo "✓ Criando tabela 'service_type'..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE TABLE IF NOT EXISTS service_type (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        name VARCHAR(50) NOT NULL UNIQUE,
        description TEXT
    );
EOSQL

echo "✓ Inserindo tipos de serviço na tabela 'service_type'"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    INSERT INTO service_type (name, description) VALUES
    ('TROCA_OLEO', 'Substituição do óleo do motor e filtro para manter o desempenho'),
    ('ALINHAMENTO', 'Ajuste da geometria das rodas para melhor dirigibilidade e economia de pneus'),
    ('BALANCEAMENTO', 'Equilibração das rodas para reduzir vibrações e desgaste irregular'),
    ('REVISAO_GERAL', 'Verificação completa dos sistemas do veículo, incluindo freios, suspensão e elétrica'),
    ('TROCA_FILTROS', 'Substituição dos filtros de ar, combustível e cabine para melhorar a eficiência'),
    ('REPARO_FREIOS', 'Manutenção e reparo do sistema de freios para segurança')
    ON CONFLICT (name) DO NOTHING;
EOSQL

echo "✓ Verificando dados inseridos em service_type..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT COUNT(*) as total_service_types FROM service_type;
    SELECT id, name, description FROM service_type ORDER BY name;
EOSQL

echo "=========================================="
echo "              TABLE SERVICE               "
echo "=========================================="

echo "✓ Criando tabela 'service'"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE TABLE IF NOT EXISTS service (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        service_type_name VARCHAR(255) NOT NULL,
        id_os UUID NOT NULL,
        service_status TEXT NOT NULL
    );
EOSQL

echo "✓ Inserindo dados mock na tabela 'service'"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    INSERT INTO service (service_type_name, id_os, service_status) VALUES
    ('TROCA_OLEO', 'a46ac51b-5ca6-439b-ba52-a36bd52e8647', '[{"status":"TO_DO","changedAt":"2026-04-30T14:35:00"}]'::jsonb);
EOSQL

echo "✓ Verificando dados inseridos em service..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT COUNT(*) as total_services FROM service;
    SELECT id, id_os, service_type_name, service_status FROM service;
EOSQL

echo "=========================================="
echo "           TABLE SERVICE_ORDER            "
echo "=========================================="

echo "✓ Criando tabela 'service_order'"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
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
EOSQL

echo "✓ Inserindo dados mock na tabela 'service_order'"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    INSERT INTO service_order (id, service_type_name, list_service, cpf_cnpj, placa) VALUES
    ('b46ac51b-5ca6-439b-ba52-a36bd52e8648', 'TROCA_OLEO', '["TROCA_OLEO", "ALINHAMENTO"]'::jsonb, '529.982.247-25', 'ABC-1234');
EOSQL

echo "✓ Verificando dados inseridos em service_order..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT COUNT(*) as total_orders FROM "service_order";
    SELECT id, service_type_name, service_status, list_service FROM "service_order";
EOSQL

echo "=========================================="
echo "            TABLE PRODUCTS                "
echo "=========================================="

echo "✓ Criando sequence e tabela 'products' (tabela base com campos compartilhados)..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
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
EOSQL

echo "✓ Inserindo mock na tabela 'parts'..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    INSERT INTO products (
        id,
        product_type,
        name,
        sku,
        unit,
        category,
        brand,
        cost_price,
        sale_price,
        active,
        created_at,
        updated_at
    ) VALUES (
        1,
        'PART',
        'Pastilha de Freio Dianteira',
        'BRK-PAD-001',
        'UNIT',
        'Freios',
        'Bosch',
        45.00,
        89.90,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
    ON CONFLICT (sku) DO UPDATE SET
        product_type = EXCLUDED.product_type,
        name = EXCLUDED.name,
        unit = EXCLUDED.unit,
        category = EXCLUDED.category,
        brand = EXCLUDED.brand,
        cost_price = EXCLUDED.cost_price,
        sale_price = EXCLUDED.sale_price,
        active = EXCLUDED.active,
        updated_at = CURRENT_TIMESTAMP;

    INSERT INTO parts (
        id,
        manufacturer_code,
        warranty_months
    ) VALUES (
        1,
        'BOH-BP-2025',
        12
    )
    ON CONFLICT (id) DO UPDATE SET
        manufacturer_code = EXCLUDED.manufacturer_code,
        warranty_months = EXCLUDED.warranty_months;

    SELECT setval('product_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM products), 1));
EOSQL

echo "✓ Verificando mock inserido em parts..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT
        p.id,
        p.name,
        p.sku,
        p.unit,
        p.category,
        p.brand,
        p.cost_price,
        p.sale_price,
        pt.manufacturer_code,
        pt.warranty_months
    FROM products p
    JOIN parts pt ON pt.id = p.id
    WHERE p.sku = 'BRK-PAD-001';
EOSQL

echo "âœ“ Inserindo mock na tabela 'supplies'..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    INSERT INTO products (
        id,
        product_type,
        name,
        sku,
        unit,
        category,
        brand,
        cost_price,
        sale_price,
        active,
        created_at,
        updated_at
    ) VALUES (
        2,
        'SUPPLY',
        'Óleo Motor 5W30 Sintético',
        'OIL-5W30-SINT',
        'LITER',
        'Lubrificantes',
        'Mobil',
        28.50,
        54.90,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
    ON CONFLICT (sku) DO UPDATE SET
        product_type = EXCLUDED.product_type,
        name = EXCLUDED.name,
        unit = EXCLUDED.unit,
        category = EXCLUDED.category,
        brand = EXCLUDED.brand,
        cost_price = EXCLUDED.cost_price,
        sale_price = EXCLUDED.sale_price,
        active = EXCLUDED.active,
        updated_at = CURRENT_TIMESTAMP;

    INSERT INTO supplies (
        id,
        fractional_allowed,
        package_size
    ) VALUES (
        2,
        TRUE,
        1.00
    )
    ON CONFLICT (id) DO UPDATE SET
        fractional_allowed = EXCLUDED.fractional_allowed,
        package_size = EXCLUDED.package_size;

    SELECT setval('product_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM products), 1));
EOSQL

echo "âœ“ Verificando mock inserido em supplies..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT
        p.id,
        p.name,
        p.sku,
        p.unit,
        p.category,
        p.brand,
        p.cost_price,
        p.sale_price,
        s.fractional_allowed,
        s.package_size
    FROM products p
    JOIN supplies s ON s.id = p.id
    WHERE p.sku = 'OIL-5W30-SINT';
EOSQL

echo "=========================================="
echo "              TABLE STOCKS                "
echo "=========================================="

echo "✓ Criando sequence e tabela 'stocks'..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
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
EOSQL

echo "✓ Inserindo mock na tabela 'stocks'..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    INSERT INTO stocks (
        product_id,
        quantity,
        reserved_quantity,
        minimum_quantity,
        created_at,
        updated_at
    ) VALUES (
        1,
        100.00,
        0.00,
        10.00,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
    ON CONFLICT (product_id) DO UPDATE SET
        quantity = EXCLUDED.quantity,
        reserved_quantity = EXCLUDED.reserved_quantity,
        minimum_quantity = EXCLUDED.minimum_quantity,
        updated_at = CURRENT_TIMESTAMP;

    SELECT setval('stock_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM stocks), 1));
EOSQL

echo "✓ Verificando mock inserido em stocks..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT id, product_id, quantity, reserved_quantity, minimum_quantity
    FROM stocks
    WHERE product_id = 1;
EOSQL

echo "=========================================="
echo "          TABLE STOCK_MOVEMENTS           "
echo "=========================================="

echo "✓ Criando sequence e tabela 'stock_movements'..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE SEQUENCE IF NOT EXISTS stock_movement_seq START WITH 1 INCREMENT BY 50;

    CREATE TABLE IF NOT EXISTS stock_movements (
        id BIGINT PRIMARY KEY DEFAULT nextval('stock_movement_seq'),
        stock_id BIGINT NOT NULL,
        type VARCHAR(50) NOT NULL,
        quantity NUMERIC(19,2) NOT NULL,
        reason VARCHAR(255),
        created_at TIMESTAMP WITHOUT TIME ZONE
    );
EOSQL

echo "=========================================="
echo "        TABLE STOCK_RESERVATIONS          "
echo "=========================================="

echo "✓ Criando sequence e tabela 'stock_reservations'..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
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
EOSQL

echo "=========================================="
echo "             TABLE BUDGETS                "
echo "=========================================="

echo "✓ Criando sequences e tabelas 'budgets' e 'budget_items'..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
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
EOSQL

echo ""
echo "=========================================="
echo "✓ Inicialização concluída com sucesso!"
echo "=========================================="

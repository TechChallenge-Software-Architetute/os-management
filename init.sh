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
        service_status VARCHAR(10) DEFAULT 'TO_DO',
        list_service VARCHAR(255) NOT NULL
    );
EOSQL

echo "✓ Inserindo dados mock na tabela 'service_order'"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    INSERT INTO service_order (id, service_type_name, list_service) VALUES
    ('b46ac51b-5ca6-439b-ba52-a36bd52e8648', 'TROCA_OLEO', '["Troca de óleo", "alinhamento"]'::jsonb);
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
        service_order_id BIGINT NOT NULL,
        quantity NUMERIC(19,2) NOT NULL,
        status VARCHAR(50) NOT NULL,
        created_at TIMESTAMP WITHOUT TIME ZONE,
        updated_at TIMESTAMP WITHOUT TIME ZONE
    );
EOSQL

echo ""
echo "=========================================="
echo "✓ Inicialização concluída com sucesso!"
echo "=========================================="

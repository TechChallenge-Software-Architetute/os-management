#!/bin/bash
set -e

echo "=========================================="
echo "Iniciando script de inicialização do PostgreSQL"
echo "=========================================="


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
        cpf VARCHAR(11) NOT NULL UNIQUE,
        email VARCHAR(255),
        phone VARCHAR(255),
        active BOOLEAN NOT NULL DEFAULT TRUE,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );
EOSQL

echo "✓ Verificando tabela clients..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT COUNT(*) as total_clients FROM clients;
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

echo "✓ Verificando tabela vehicles..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT COUNT(*) as total_vehicles FROM vehicles;
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

echo ""
echo "=========================================="
echo "✓ Inicialização concluída com sucesso!"
echo "=========================================="

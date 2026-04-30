#!/bin/bash
set -e

echo "=========================================="
echo "Iniciando script de inicialização do PostgreSQL"
echo "=========================================="

# "✓ Criando tabela 'service_type'..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE TABLE IF NOT EXISTS service_type (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        name VARCHAR(50) NOT NULL UNIQUE,
        description TEXT
    );
EOSQL
# "✓ Tabela 'service_type' criada com sucesso!"

# "✓ Inserindo tipos de serviço na tabela 'service_type'
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

# ✓ Criando tabela 'service'
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE TABLE IF NOT EXISTS service (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        service_type_name VARCHAR(255) NOT NULL,
        id_os UUID NOT NULL,
        service_status VARCHAR(10) DEFAULT 'TO_DO',
        created_at TIMESTAMP DEFAULT NOW()
    );
EOSQL

# "✓ Inserindo dados mock na tabela 'service'
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    INSERT INTO service (service_type_name, id_os) VALUES
    ('TROCA_OLEO', 'a46ac51b-5ca6-439b-ba52-a36bd52e8647');
EOSQL

echo "✓ Verificando dados inseridos em service..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT COUNT(*) as total_services FROM service;
    SELECT id, id_os, service_type_name, service_status, created_at FROM service;
EOSQL

# ✓ Criando tabela 'service_order'
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE TABLE IF NOT EXISTS service_order (
        id UUID PRIMARY KEY,
        service_type_name VARCHAR(255) NOT NULL,
        service_status VARCHAR(10) DEFAULT 'TO_DO',
        list_service VARCHAR(255) NOT NULL
    );
EOSQL

# ✓ Inserindo dados mock na tabela 'service_order'
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

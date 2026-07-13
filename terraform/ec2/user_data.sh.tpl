#!/bin/bash
set -e

# Atualiza pacotes e instala Docker + cliente PostgreSQL
apt-get update -y
apt-get install -y docker.io postgresql-client
systemctl start docker
systemctl enable docker

# Aguarda o RDS aceitar conexoes (pode demorar alguns segundos apos o Terraform aplicar)
sleep 30

# Sobe o container da aplicacao
docker run -d \
  --name os-management \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="${db_url}" \
  -e SPRING_DATASOURCE_USERNAME="${db_user}" \
  -e SPRING_DATASOURCE_PASSWORD="${db_password}" \
  -e JWT_SECRET="${jwt_secret}" \
  -e JWT_EXPIRATION="86400000" \
  -e SERVER_PORT="8080" \
  -e SPRING_APPLICATION_NAME="workshop" \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO="update" \
  -e SPRING_JPA_SHOW_SQL="false" \
  -e AWS_REGION="${aws_region}" \
  -e AWS_SNS_TOPIC_ARN="${sns_topic_arn}" \
  ${app_image}

echo "Container iniciado: $(docker ps --format '{{.Names}} {{.Status}}')" >> /var/log/os-management.log

# Aguarda o Spring Boot subir — Hibernate precisa criar as tabelas antes do seed
echo "Aguardando Spring Boot inicializar..." >> /var/log/os-management.log
i=0
until curl -s -o /dev/null http://localhost:8080; do
  i=$((i + 1))
  if [ "$i" -ge 60 ]; then
    echo "ERRO: Spring Boot nao respondeu em 60 tentativas" >> /var/log/os-management.log
    exit 1
  fi
  sleep 5
done
echo "Spring Boot respondendo. Executando seed do banco..." >> /var/log/os-management.log

# Extrai host do RDS a partir do JDBC URL (jdbc:postgresql://HOST:5432/db)
DB_HOST=$(echo "${db_url}" | sed 's|jdbc:postgresql://||' | cut -d':' -f1)

# Seed inicial: roles, superadmin e dados de exemplo
PGPASSWORD="${db_password}" psql -h "$DB_HOST" -U "${db_user}" -d "${db_name}" <<'EOSQL'
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Sequences que o Hibernate nao cria automaticamente
CREATE SEQUENCE IF NOT EXISTS clients_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS vehicles_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS product_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS stock_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS stock_movement_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS stock_reservation_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS budget_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS budget_item_seq START WITH 1 INCREMENT BY 50;

-- Roles
INSERT INTO roles (id, name) VALUES
  (uuid_generate_v4(), 'ROLE_ADMIN'),
  (uuid_generate_v4(), 'ROLE_USER'),
  (uuid_generate_v4(), 'ROLE_TECHNICIAN')
ON CONFLICT (name) DO NOTHING;

-- Superadmin (senha: coxinha123)
INSERT INTO users (id, email, password)
VALUES (uuid_generate_v4(), 'superadmin@system.com', '$2a$12$RJVIgDQpKX6.CtZiY9BQB.RNqNiDU7Y0Y6AMMlLUrxyApokRvMVrC')
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'superadmin@system.com'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur JOIN users u2 ON u2.id = ur.user_id WHERE u2.email = 'superadmin@system.com'
  );

-- Clients
INSERT INTO clients (id, name, document, email, phone, active, created_at, updated_at)
SELECT 1,'JOAO DA SILVA','52998224725','joao.silva@email.com','(11) 99999-1234',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM clients WHERE document = '52998224725');

INSERT INTO clients (id, name, document, email, phone, active, created_at, updated_at)
SELECT 2,'MARIA SOUZA','07124632080','maria.souza@email.com','(21) 98888-5678',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM clients WHERE document = '07124632080');

INSERT INTO clients (id, name, document, email, phone, active, created_at, updated_at)
SELECT 3,'CARLOS OLIVEIRA','18746880011','carlos.oliveira@email.com','(31) 97777-9012',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM clients WHERE document = '18746880011');

SELECT setval('clients_seq', GREATEST((SELECT COALESCE(MAX(id),1) FROM clients),1));

-- Vehicles
INSERT INTO vehicles (id, client_id, plate, brand, model, year, color, type, active, created_at, updated_at)
SELECT 1,c.id,'ABC1234','TOYOTA','COROLLA',2020,'PRATA','CAR',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP
FROM clients c WHERE c.document='52998224725'
  AND NOT EXISTS (SELECT 1 FROM vehicles WHERE plate='ABC1234');

INSERT INTO vehicles (id, client_id, plate, brand, model, year, color, type, active, created_at, updated_at)
SELECT 2,c.id,'XYZ1A23','HONDA','CIVIC',2021,'PRETO','CAR',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP
FROM clients c WHERE c.document='52998224725'
  AND NOT EXISTS (SELECT 1 FROM vehicles WHERE plate='XYZ1A23');

INSERT INTO vehicles (id, client_id, plate, brand, model, year, color, type, active, created_at, updated_at)
SELECT 3,c.id,'DEF5678','VOLKSWAGEN','GOL',2019,'BRANCO','CAR',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP
FROM clients c WHERE c.document='07124632080'
  AND NOT EXISTS (SELECT 1 FROM vehicles WHERE plate='DEF5678');

SELECT setval('vehicles_seq', GREATEST((SELECT COALESCE(MAX(id),1) FROM vehicles),1));

-- Service types
INSERT INTO service_type (name, description) VALUES
  ('TROCA_OLEO',   'Substituicao do oleo do motor e filtro'),
  ('ALINHAMENTO',  'Ajuste da geometria das rodas'),
  ('BALANCEAMENTO','Equilibracao das rodas'),
  ('REVISAO_GERAL','Verificacao completa dos sistemas do veiculo'),
  ('TROCA_FILTROS','Substituicao dos filtros de ar, combustivel e cabine'),
  ('REPARO_FREIOS','Manutencao e reparo do sistema de freios')
ON CONFLICT (name) DO NOTHING;

-- Products + Parts + Supplies
INSERT INTO products (id, product_type, name, sku, unit, category, brand, cost_price, sale_price, active, created_at, updated_at)
VALUES (1,'PART','Pastilha de Freio Dianteira','BRK-PAD-001','UNIT','Freios','Bosch',45.00,89.90,TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
ON CONFLICT (sku) DO UPDATE SET updated_at=CURRENT_TIMESTAMP;

INSERT INTO parts (id, manufacturer_code, warranty_months)
VALUES (1,'BOH-BP-2025',12) ON CONFLICT (id) DO UPDATE SET manufacturer_code=EXCLUDED.manufacturer_code;

INSERT INTO products (id, product_type, name, sku, unit, category, brand, cost_price, sale_price, active, created_at, updated_at)
VALUES (2,'SUPPLY','Oleo Motor 5W30 Sintetico','OIL-5W30-SINT','LITER','Lubrificantes','Mobil',28.50,54.90,TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
ON CONFLICT (sku) DO UPDATE SET updated_at=CURRENT_TIMESTAMP;

INSERT INTO supplies (id, fractional_allowed, package_size)
VALUES (2,TRUE,1.00) ON CONFLICT (id) DO UPDATE SET fractional_allowed=EXCLUDED.fractional_allowed;

SELECT setval('product_seq', GREATEST((SELECT COALESCE(MAX(id),1) FROM products),1));

-- Stocks
INSERT INTO stocks (product_id, quantity, reserved_quantity, minimum_quantity, created_at, updated_at)
VALUES (1,100.00,0.00,10.00,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
ON CONFLICT (product_id) DO UPDATE SET updated_at=CURRENT_TIMESTAMP;

SELECT setval('stock_seq', GREATEST((SELECT COALESCE(MAX(id),1) FROM stocks),1));
EOSQL

echo "Seed concluido com sucesso!" >> /var/log/os-management.log

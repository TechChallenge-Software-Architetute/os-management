-- Converted from the former PostgreSQL init scripts.
-- Database creation/destruction is intentionally outside Flyway.

INSERT INTO roles (id, name)
SELECT uuid_generate_v4(), role_name
FROM (VALUES
    ('ROLE_ADMIN'),
    ('ROLE_USER'),
    ('ROLE_TECHNICIAN')
) AS seed(role_name)
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = seed.role_name
);

UPDATE users
SET password = '$2a$12$RJVIgDQpKX6.CtZiY9BQB.RNqNiDU7Y0Y6AMMlLUrxyApokRvMVrC'
WHERE email = 'superadmin@system.com';

INSERT INTO users (id, email, password)
SELECT
    uuid_generate_v4(),
    'superadmin@system.com',
    '$2a$12$RJVIgDQpKX6.CtZiY9BQB.RNqNiDU7Y0Y6AMMlLUrxyApokRvMVrC' --coxinha123
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'superadmin@system.com'
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'superadmin@system.com'
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );

INSERT INTO clients (id, name, document, cpf, email, phone, active, created_at, updated_at)
SELECT 1, 'JOAO DA SILVA', '52998224725', '52998224725', 'joao.silva@email.com', '(11) 99999-1234', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM clients WHERE document = '52998224725');

INSERT INTO clients (id, name, document, cpf, email, phone, active, created_at, updated_at)
SELECT 2, 'MARIA SOUZA', '07124632080', '07124632080', 'maria.souza@email.com', '(21) 98888-5678', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM clients WHERE document = '07124632080');

INSERT INTO clients (id, name, document, cpf, email, phone, active, created_at, updated_at)
SELECT 3, 'CARLOS OLIVEIRA', '18746880011', '18746880011', 'carlos.oliveira@email.com', '(31) 97777-9012', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM clients WHERE document = '18746880011');

SELECT setval('clients_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM clients), 1));

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

INSERT INTO service_type (name, description) VALUES
('TROCA_OLEO', 'Substituição do óleo do motor e filtro para manter o desempenho'),
('ALINHAMENTO', 'Ajuste da geometria das rodas para melhor dirigibilidade e economia de pneus'),
('BALANCEAMENTO', 'Equilibração das rodas para reduzir vibrações e desgaste irregular'),
('REVISAO_GERAL', 'Verificação completa dos sistemas do veículo, incluindo freios, suspensão e elétrica'),
('TROCA_FILTROS', 'Substituição dos filtros de ar, combustível e cabine para melhorar a eficiência'),
('REPARO_FREIOS', 'Manutenção e reparo do sistema de freios para segurança')
ON CONFLICT (name) DO NOTHING;

INSERT INTO service (service_type_name, id_os, service_status)
SELECT 'TROCA_OLEO', 'a46ac51b-5ca6-439b-ba52-a36bd52e8647', '[{"status":"TO_DO","changedAt":"2026-04-30T14:35:00"}]'
WHERE NOT EXISTS (
    SELECT 1 FROM service WHERE id_os = 'a46ac51b-5ca6-439b-ba52-a36bd52e8647'
);

INSERT INTO service_order (id, service_type_name, list_service, cpf_cnpj, placa) VALUES
('b46ac51b-5ca6-439b-ba52-a36bd52e8648', 'TROCA_OLEO', '["TROCA_OLEO", "ALINHAMENTO"]', '529.982.247-25', 'ABC-1234')
ON CONFLICT (id) DO UPDATE SET
    service_type_name = EXCLUDED.service_type_name,
    list_service = EXCLUDED.list_service,
    cpf_cnpj = EXCLUDED.cpf_cnpj,
    placa = EXCLUDED.placa,
    updated_at = NOW();

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

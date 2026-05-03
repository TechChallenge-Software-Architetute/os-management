# 🔧 OS Management — Sistema de Gestão de Ordens de Serviço para Oficina Mecânica

Este projeto foi desenvolvido como parte do programa de **Pós-Graduação da FIAP** (Pós Tech).

Uma oficina mecânica local vinha utilizando planilhas e anotações manuais para gerenciar suas operações do dia a dia. Com o tempo, essa abordagem gerou uma série de problemas recorrentes:

- Dificuldade em priorizar atendimentos, causando atrasos e insatisfação dos clientes
- Falta de controle confiável sobre o estoque de peças e insumos, gerando rupturas e compras excessivas
- Dificuldade em acompanhar o status em tempo real dos serviços em andamento em múltiplos veículos
- Perda do histórico de clientes e veículos, dificultando o atendimento em visitas recorrentes
- Ineficiência no fluxo de orçamento e aprovação, com cálculos manuais e sem registro do valor no momento da aprovação

Para resolver esses problemas, desenvolvemos o **OS Management** — um sistema completo de gestão de ordens de serviço projetado para digitalizar e otimizar as operações da oficina. O sistema cobre todo o ciclo de vida de uma ordem de serviço, desde o recebimento até a entrega do veículo.

**Principais funcionalidades:**

- Cadastro de clientes e veículos com CRUD completo e soft delete
- Catálogo de peças e insumos com busca por SKU e controle de estoque
- Gestão de estoque com rastreamento de entradas/saídas, alertas de estoque mínimo e histórico de movimentações
- Reservas atômicas de estoque vinculadas a ordens de serviço, evitando alocação duplicada
- Geração automática de orçamento baseado nos produtos reservados, com snapshot de preços congelado no momento da aprovação
- Gestão do ciclo de vida da ordem de serviço (recebida → diagnóstico → aguardando aprovação → aprovada → finalizada → entregue)
- Acompanhamento individual do status de cada serviço dentro da OS (a fazer → em andamento → concluído)
- Monitoramento de tempo de execução dos serviços para análise de desempenho
- Autenticação via JWT e controle de acesso baseado em roles
- Script de validação end-to-end simulando o fluxo completo da oficina

> **Projeto de Pós-Graduação** — FIAP Pós Tech

---

## 📑 Sumário

- [Visão Geral da Arquitetura](#-visão-geral-da-arquitetura)
- [Tecnologias](#-tecnologias)
- [Pré-requisitos e Como Executar](#-pré-requisitos-e-como-executar)
- [Endpoints da API](#-endpoints-da-api)
- [Usando o Bruno para Testar a API](#-usando-o-bruno-para-testar-a-api)
- [Scripts](#-scripts)

---

## 🏗 Visão Geral da Arquitetura

O projeto adota a **Vertical Slice Architecture**, onde cada funcionalidade (feature) é organizada como uma fatia vertical independente, contendo seus próprios controllers, handlers, requests, responses, domínio e repositórios. Isso promove alta coesão e baixo acoplamento entre as features.

### Estrutura de Diretórios

```
src/main/java/com/os/workshop/
├── features/
│   ├── budget/              # Orçamento automático (gerado a partir das reservas de estoque)
│   ├── client/              # Cadastro e gestão de clientes (CRUD + soft delete)
│   ├── monitoring/          # Monitoramento de tempo médio de execução por tipo de serviço
│   ├── product/             # Peças e insumos
│   │   ├── part/            # CRUD de peças (ex: pastilha de freio)
│   │   ├── supply/          # CRUD de insumos (ex: óleo 5W30)
│   │   ├── domain/          # Classes de domínio compartilhadas
│   │   ├── persistence/     # Entidades JPA, mappers e adapters
│   │   └── repository/      # Interfaces de porta (ports)
│   ├── service/             # Serviços vinculados à OS (TROCA_OLEO, ALINHAMENTO, etc.)
│   ├── serviceorder/        # Ordens de serviço
│   │   ├── create/          # Handler, request e response de criação
│   │   ├── update/          # Atualização de status da OS
│   │   ├── findById/        # Consulta por ID
│   │   ├── list/            # Listagem de todas as OS
│   │   └── shared/          # Domínio, repositório e mapper compartilhados
│   ├── stock/               # Controle de estoque
│   │   ├── management/      # Entrada e saída de estoque
│   │   └── reservation/     # Reserva atômica de estoque para OS
│   ├── user/                # Autenticação e autorização via JWT
│   ├── utils/               # Anotações e utilitários compartilhados
│   └── vehicle/             # Cadastro e gestão de veículos
├── WorkshopApplication.java # Classe principal da aplicação
└── DataInitializer.java     # Inicializador de dados
```

### Fluxo Principal da Ordem de Serviço

```
RECEBIDA → EM_DIAGNOSTICO → AGUARDANDO_APROVACAO → APROVADO → FINALIZADA → ENTREGUE
```

1. **RECEBIDA** — OS criada com os serviços solicitados
2. **EM_DIAGNOSTICO** — Mecânico avalia o veículo e reserva peças/insumos do estoque
3. **AGUARDANDO_APROVACAO** — Orçamento gerado automaticamente; aguarda aprovação do cliente
4. **APROVADO** — Cliente aprova; mecânico executa os serviços (TO_DO → DOING → DONE)
5. **FINALIZADA** — Todos os serviços concluídos; reservas de estoque confirmadas
6. **ENTREGUE** — Veículo devolvido ao cliente

---

## 🛠 Tecnologias

| Tecnologia | Versão | Finalidade |
|---|---|---|
| **Java** | 21 | Linguagem principal |
| **Spring Boot** | 4.0.5 | Framework web e DI |
| **Spring Security** | — | Autenticação e autorização |
| **Spring Data JPA** | — | Persistência de dados |
| **PostgreSQL** | 16 | Banco de dados relacional |
| **Docker / Docker Compose** | — | Containerização e orquestração |
| **Maven** | — | Gerenciamento de dependências e build |
| **MapStruct** | 1.6.3 | Mapeamento entre DTOs e entidades |
| **Lombok** | — | Redução de boilerplate |
| **JWT (jjwt)** | 0.13.0 | Tokens de autenticação |
| **SpringDoc OpenAPI** | 3.0.2 | Documentação Swagger UI |
| **JaCoCo** | 0.8.14 | Cobertura de testes |
| **SonarQube** | Community | Análise estática de código |

### Banco de Dados

Optamos por um banco de dados **relacional (SQL)** — especificamente o **PostgreSQL** — ao invés de uma solução NoSQL. Essa escolha foi motivada pelas características do domínio da aplicação:

- **JOINs entre múltiplas entidades:** O sistema possui relacionamentos complexos entre clientes, veículos, ordens de serviço, serviços, produtos, estoque, reservas e orçamentos. Consultas que cruzam essas informações (ex: "listar todas as OS de um cliente com seus respectivos orçamentos e produtos reservados") são naturalmente expressas com JOINs SQL, que seriam significativamente mais complexas em bancos NoSQL.
- **Integridade referencial:** Foreign keys garantem que não existam reservas de estoque para produtos inexistentes, nem ordens de serviço para clientes removidos. O banco impede inconsistências no nível de dados.
- **Transações ACID:** Operações como a reserva atômica de estoque (onde múltiplos produtos são reservados em uma única transação, ou nenhum é) dependem de transações com garantias de atomicidade e isolamento que bancos SQL oferecem nativamente.
- **Schema estruturado:** O domínio da oficina possui entidades bem definidas com campos fixos e tipos conhecidos. Não há necessidade de flexibilidade de schema — ao contrário, a rigidez do schema SQL previne erros de dados.
- **Consultas complexas e agregações:** Funcionalidades como monitoramento de tempo médio de execução por tipo de serviço e relatórios de estoque baixo se beneficiam da expressividade do SQL para agregações, filtros e ordenações.

---

## 🚀 Pré-requisitos e Como Executar

### Pré-requisitos

- **Java 21** (JDK) instalado
- **Docker** e **Docker Compose** instalados
- Porta **5432** (PostgreSQL) e **8080** (aplicação) disponíveis
- Porta **9000** disponível (opcional, para SonarQube)

### Passo a Passo

**1. Clone o repositório:**

```bash
git clone <url-do-repositorio>
cd os-management
```

**2. Suba o banco de dados com Docker Compose:**

```bash
docker compose up -d
```

Isso irá iniciar:
- **PostgreSQL 16** na porta `5432` (usuário: `user`, senha: `password`, banco: `workshop`)
- **SonarQube** na porta `9000` (opcional, para análise de código)

> O script `init.sh` é executado automaticamente pelo PostgreSQL na primeira inicialização, criando todas as tabelas e inserindo dados de exemplo (usuários, clientes, veículos, tipos de serviço, peças, insumos e estoques).

**3. Execute a aplicação:**

```bash
./mvnw spring-boot:run
```

> **Nota:** O container da aplicação está comentado no `docker-compose.yml`. A aplicação deve ser executada localmente com o comando acima.

**4. Acesse a aplicação:**

| Recurso | URL |
|---|---|
| API Base | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| SonarQube | http://localhost:9000 |

**5. Credenciais do Super Admin:**

| Campo | Valor |
|---|---|
| Email | `superadmin@system.com` |
| Senha | `coxinha123` |

### Executar Testes

```bash
./mvnw test
```

### Gerar Relatório de Cobertura (JaCoCo)

```bash
./mvnw test jacoco:report
```

O relatório será gerado em `target/site/jacoco/index.html`.

### Análise com SonarQube

```bash
./mvnw sonar:sonar
```

> Certifique-se de que o SonarQube está rodando em `http://localhost:9000`.

---

## 📡 Endpoints da API

> **Importante:** Todos os endpoints (exceto `/auth/login` e `/signup`) exigem o header `Authorization: Bearer <token>`.

### 1. Autenticação

#### `POST /auth/login` — Realizar login

**Request:**
```json
{
  "email": "superadmin@system.com",
  "password": "coxinha123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "expiresIn": 3600000
}
```

#### `POST /signup` — Cadastrar novo usuário (requer ROLE_ADMIN)

**Request:**
```json
{
  "email": "mecanico@oficina.com",
  "password": "senha123",
  "roles": ["ROLE_TECHNICIAN"]
}
```

---

### 2. Clientes

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/clients` | Cadastrar cliente |
| `GET` | `/api/clients` | Listar todos os clientes ativos |
| `GET` | `/api/clients/{id}` | Buscar cliente por ID |
| `GET` | `/api/clients/cpf/{cpf}` | Buscar cliente por CPF |
| `PUT` | `/api/clients/{id}` | Atualizar cliente |
| `DELETE` | `/api/clients/{id}` | Desativar cliente (soft delete) |

#### `POST /api/clients` — Cadastrar cliente

**Request:**
```json
{
  "name": "JOÃO DA SILVA",
  "cpf": "52998224725",
  "email": "joao.silva@email.com",
  "phone": "(11) 99999-1234"
}
```

**Response (201):**
```json
{
  "id": 1,
  "name": "JOÃO DA SILVA",
  "cpf": "52998224725",
  "email": "joao.silva@email.com",
  "phone": "(11) 99999-1234",
  "active": true,
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00"
}
```

---

### 3. Veículos

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/vehicles` | Cadastrar veículo |
| `GET` | `/api/vehicles/{id}` | Buscar veículo por ID |
| `GET` | `/api/vehicles/plate/{plate}` | Buscar veículo por placa |
| `GET` | `/api/vehicles/client/{clientId}` | Listar veículos de um cliente |
| `PUT` | `/api/vehicles/{id}` | Atualizar veículo |
| `DELETE` | `/api/vehicles/{id}` | Desativar veículo (soft delete) |

#### `POST /api/vehicles` — Cadastrar veículo

**Request:**
```json
{
  "clientId": 1,
  "plate": "ABC1234",
  "brand": "TOYOTA",
  "model": "COROLLA",
  "year": 2020,
  "color": "PRATA",
  "type": "CAR"
}
```

**Response (201):**
```json
{
  "id": 1,
  "clientId": 1,
  "plate": "ABC1234",
  "brand": "TOYOTA",
  "model": "COROLLA",
  "year": 2020,
  "color": "PRATA",
  "type": "CAR",
  "active": true,
  "createdAt": "2025-01-15T10:35:00",
  "updatedAt": "2025-01-15T10:35:00"
}
```

---

### 4. Peças (Parts)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/parts` | Cadastrar peça |
| `GET` | `/api/parts` | Listar todas as peças ativas |
| `GET` | `/api/parts/{id}` | Buscar peça por ID |
| `GET` | `/api/parts/sku/{sku}` | Buscar peça por SKU |
| `PUT` | `/api/parts/{id}` | Atualizar peça |
| `DELETE` | `/api/parts/{id}` | Desativar peça (soft delete) |

#### `POST /api/parts` — Cadastrar peça

**Request:**
```json
{
  "name": "Pastilha de Freio Dianteira",
  "sku": "BRK-PAD-001",
  "unit": "UNIT",
  "category": "Freios",
  "brand": "Bosch",
  "costPrice": 45.00,
  "salePrice": 89.90,
  "manufacturerCode": "BOH-BP-2025",
  "warrantyMonths": 12
}
```

**Response (201):**
```json
{
  "id": 1,
  "name": "Pastilha de Freio Dianteira",
  "sku": "BRK-PAD-001",
  "unit": "UNIT",
  "category": "Freios",
  "brand": "Bosch",
  "costPrice": 45.00,
  "salePrice": 89.90,
  "manufacturerCode": "BOH-BP-2025",
  "warrantyMonths": 12,
  "active": true,
  "createdAt": "2025-01-15T10:40:00",
  "updatedAt": "2025-01-15T10:40:00"
}
```

---

### 5. Insumos (Supplies)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/supplies` | Cadastrar insumo |
| `GET` | `/api/supplies` | Listar todos os insumos ativos |
| `GET` | `/api/supplies/{id}` | Buscar insumo por ID |
| `GET` | `/api/supplies/sku/{sku}` | Buscar insumo por SKU |
| `PUT` | `/api/supplies/{id}` | Atualizar insumo |
| `DELETE` | `/api/supplies/{id}` | Desativar insumo (soft delete) |

#### `POST /api/supplies` — Cadastrar insumo

**Request:**
```json
{
  "name": "Óleo Motor 5W30 Sintético",
  "sku": "OIL-5W30-SINT",
  "unit": "LITER",
  "category": "Lubrificantes",
  "brand": "Mobil",
  "costPrice": 28.50,
  "salePrice": 54.90,
  "fractionalAllowed": true,
  "packageSize": 1.00
}
```

**Response (201):**
```json
{
  "id": 2,
  "name": "Óleo Motor 5W30 Sintético",
  "sku": "OIL-5W30-SINT",
  "unit": "LITER",
  "category": "Lubrificantes",
  "brand": "Mobil",
  "costPrice": 28.50,
  "salePrice": 54.90,
  "fractionalAllowed": true,
  "packageSize": 1.00,
  "active": true,
  "createdAt": "2025-01-15T10:45:00",
  "updatedAt": "2025-01-15T10:45:00"
}
```

---

### 6. Estoque (Stock)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/stocks` | Criar estoque para um produto |
| `GET` | `/api/stocks` | Listar todos os estoques |
| `GET` | `/api/stocks/low` | Listar estoques abaixo do mínimo |
| `GET` | `/api/stocks/product/{productId}` | Buscar estoque por produto |
| `PATCH` | `/api/stocks/product/{productId}/entry` | Entrada de estoque |
| `PATCH` | `/api/stocks/product/{productId}/exit` | Saída de estoque |
| `PATCH` | `/api/stocks/product/{productId}/minimum?minimumQuantity=X` | Atualizar quantidade mínima |
| `GET` | `/api/stocks/product/{productId}/movements` | Histórico de movimentações |

#### `POST /api/stocks` — Criar estoque para um produto

**Request:**
```json
{
  "productId": 1,
  "quantity": 100.00,
  "minimumQuantity": 10.00
}
```

**Response (201):**
```json
{
  "id": 1,
  "productId": 1,
  "quantity": 100.00,
  "reservedQuantity": 0.00,
  "minimumQuantity": 10.00,
  "createdAt": "2025-01-15T10:50:00",
  "updatedAt": "2025-01-15T10:50:00"
}
```

#### `PATCH /api/stocks/product/{productId}/entry` — Entrada de estoque

**Request:**
```json
{
  "quantity": 50.00,
  "reason": "Reposição de estoque mensal"
}
```

#### `PATCH /api/stocks/product/{productId}/exit` — Saída de estoque

**Request:**
```json
{
  "quantity": 5.00,
  "reason": "Uso interno"
}
```

#### `PATCH /api/stocks/product/{productId}/minimum?minimumQuantity=10` — Atualizar quantidade mínima

> Enviar o parâmetro `minimumQuantity` como query parameter na URL.

---

### 7. Reservas de Estoque (Stock Reservations)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/stocks/reservations` | Reservar estoque para uma OS (operação atômica) |
| `PATCH` | `/api/stocks/reservations/service-order/{serviceOrderId}/confirm` | Confirmar reservas da OS |
| `PATCH` | `/api/stocks/reservations/service-order/{serviceOrderId}/release` | Liberar reservas da OS |
| `GET` | `/api/stocks/reservations/service-order/{serviceOrderId}` | Listar reservas de uma OS |

#### `POST /api/stocks/reservations` — Reservar estoque para OS

> A reserva é **atômica**: ou todos os itens são reservados com sucesso, ou nenhum é reservado.

**Request:**
```json
{
  "serviceOrderId": "b46ac51b-5ca6-439b-ba52-a36bd52e8648",
  "items": [
    {
      "productId": 1,
      "quantity": 2.00
    },
    {
      "productId": 2,
      "quantity": 4.00
    }
  ]
}
```

**Response (201):**
```json
[
  {
    "id": 1,
    "stockId": 1,
    "productId": 1,
    "serviceOrderId": "b46ac51b-5ca6-439b-ba52-a36bd52e8648",
    "quantity": 2.00,
    "status": "RESERVED",
    "createdAt": "2025-01-15T11:00:00",
    "updatedAt": "2025-01-15T11:00:00"
  },
  {
    "id": 2,
    "stockId": 2,
    "productId": 2,
    "serviceOrderId": "b46ac51b-5ca6-439b-ba52-a36bd52e8648",
    "quantity": 4.00,
    "status": "RESERVED",
    "createdAt": "2025-01-15T11:00:00",
    "updatedAt": "2025-01-15T11:00:00"
  }
]
```

---

### 8. Ordens de Serviço (Service Orders)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/order` | Criar ordem de serviço |
| `GET` | `/order` | Listar todas as OS (inclui orçamento) |
| `GET` | `/order/{id}` | Buscar OS por ID (inclui orçamento) |
| `PATCH` | `/order/{id}` | Atualizar status da OS |

#### `POST /order` — Criar ordem de serviço

**Request:**
```json
{
  "cpfCnpj": "529.982.247-25",
  "placaVeiculo": "ABC-1234",
  "serviceTypes": [
    "TROCA_OLEO",
    "ALINHAMENTO"
  ]
}
```

**Response (201):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "serviceTypeName": "TROCA_OLEO",
  "serviceStatus": "RECEBIDA",
  "listService": ["TROCA_OLEO", "ALINHAMENTO"],
  "cpfCnpj": "529.982.247-25",
  "placa": "ABC-1234"
}
```

#### `PATCH /order/{id}` — Atualizar status da OS

**Request:**
```json
{
  "status": "EM_DIAGNOSTICO"
}
```

> **Status válidos (em ordem):** `RECEBIDA` → `EM_DIAGNOSTICO` → `AGUARDANDO_APROVACAO` → `APROVADO` → `FINALIZADA` → `ENTREGUE`

---

### 9. Serviços (Services)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/services` | Criar serviço |
| `GET` | `/services` | Listar todos os serviços |
| `GET` | `/services/{id}` | Buscar serviço por ID |
| `GET` | `/services/os/{idOS}` | Listar serviços de uma OS |
| `PATCH` | `/services/update-status` | Atualizar status do serviço |
| `GET` | `/service-types` | Listar tipos de serviço disponíveis |

#### `PATCH /services/update-status` — Atualizar status do serviço

**Request:**
```json
{
  "id": "uuid-do-servico",
  "status": "DOING"
}
```

> **Status do serviço:** `TO_DO` → `DOING` → `DONE`

---

### 10. Orçamento (Budget)

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/budgets/service-order/{serviceOrderId}` | Consultar orçamento da OS |

> O orçamento é **gerado automaticamente** com base nas reservas de estoque da OS. Cada item reservado aparece no orçamento com seu preço de venda e quantidade.

#### `GET /api/budgets/service-order/{serviceOrderId}` — Consultar orçamento

**Response (200):**
```json
{
  "id": 1,
  "serviceOrderId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "totalPrice": 399.40,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Pastilha de Freio Dianteira",
      "productSku": "BRK-PAD-001",
      "productType": "PART",
      "quantity": 2.00,
      "unitPrice": 89.90,
      "totalPrice": 179.80
    },
    {
      "id": 2,
      "productId": 2,
      "productName": "Óleo Motor 5W30 Sintético",
      "productSku": "OIL-5W30-SINT",
      "productType": "SUPPLY",
      "quantity": 4.00,
      "unitPrice": 54.90,
      "totalPrice": 219.60
    }
  ],
  "createdAt": "2025-01-15T11:05:00",
  "updatedAt": "2025-01-15T11:05:00"
}
```

---

### 11. Monitoramento (Monitoring)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/monitoring/all` | Tempo médio de execução por tipo de serviço |

#### `POST /monitoring/all` — Consultar tempo médio de execução

**Request:**
```json
{
  "timeUnit": "SECONDS"
}
```

**Response (200):**
```json
[
  {
    "serviceType": "TROCA_OLEO",
    "averageTime": 45.5,
    "timeUnit": "SECONDS"
  },
  {
    "serviceType": "ALINHAMENTO",
    "averageTime": 72.3,
    "timeUnit": "SECONDS"
  }
]
```

---

## 🧪 Usando o Bruno para Testar a API

O [Bruno](https://www.usebruno.com/) é um cliente HTTP open-source para testar APIs, similar ao Postman, mas com armazenamento local dos dados. Abaixo está um guia passo a passo para configurar e testar todos os endpoints do sistema.

### 1. Download e Instalação

1. Acesse [https://www.usebruno.com/downloads](https://www.usebruno.com/downloads)
2. Baixe a versão compatível com seu sistema operacional (Windows, macOS ou Linux)
3. Instale e abra o Bruno

### 2. Importar a Collection

1. Na tela inicial do Bruno, clique em **"Import Collection"** ou **"Import"**
2. Na tela **"Drop file(s) to import or choose file(s)"**, clique em **"choose file(s)"**
3. Navegue até a pasta `bruno` na raiz do projeto:
   ```text
   os-management/bruno/
   ```
4. Selecione o arquivo:
   ```text
   os-management-api.bruno.auto.json
   ```
5. Confirme a importação com o **"File Format Bruno**"
6. O Bruno criará a collection **"os-management-api"** com as chamadas HTTP prontas para testar

### 3. Primeira Chamada: Login para Obter o Token

Esta é a chamada mais importante — ela retorna o token JWT necessário para todas as outras requisições.

1. Dentro da collection, clique no botão **"+"** (ou clique com o botão direito → **"New Request"**)
2. Nomeie a request como **"Login"**
3. Selecione o método **POST**
4. No campo de URL, digite:
   ```
   http://localhost:8080/auth/login
   ```
5. Vá até a aba **"Body"**
6. Selecione **"JSON"** no dropdown
7. Cole o seguinte JSON:
   ```json
   {
     "email": "superadmin@system.com",
     "password": "coxinha123"
   }
   ```
8. Clique no botão **"Send"** (ícone ▶ ou atalho Ctrl+Enter)
9. Na resposta, você verá algo como:
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdXBlcm...",
     "type": "Bearer",
     "expiresIn": 3600000
   }
   ```
10. **Copie o valor do campo `token`** — você vai precisar dele nos próximos passos

### 4. Configurar o Bearer Token para Requisições Autenticadas

Para cada nova request que exigir autenticação:

1. Crie uma nova request (botão **"+"**)
2. Após definir o método e a URL, vá até a aba **"Auth"**
3. No dropdown de tipo de autenticação, selecione **"Bearer Token"**
4. No campo que aparece, cole o token copiado do login:
   ```
   eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdXBlcm...
   ```
5. Pronto! O Bruno adicionará automaticamente o header `Authorization: Bearer <token>` em cada requisição

> **Dica:** Você pode configurar o token no nível da collection para não precisar repetir em cada request. Clique com o botão direito na collection → **"Settings"** → aba **"Auth"** → selecione **"Bearer Token"** e cole o token. Todas as requests herdarão essa configuração.

### 5. Como Adicionar um Body JSON

Para requisições que enviam dados (POST, PUT, PATCH):

1. Na request, vá até a aba **"Body"**
2. No dropdown, selecione **"JSON"**
3. Cole o JSON no editor que aparece. Exemplo para criar um cliente:
   ```json
   {
     "name": "MARIA SOUZA",
     "cpf": "07124632080",
     "email": "maria.souza@email.com",
     "phone": "(21) 98888-5678"
   }
   ```
4. Clique em **"Send"**

### 6. Como Usar Variáveis de Path (URL)

Para endpoints com parâmetros na URL como `{id}`, `{serviceOrderId}`, `{cpf}`, etc.:

1. Substitua o parâmetro diretamente na URL. Exemplos:
   - Buscar cliente por ID:
     ```
     http://localhost:8080/api/clients/1
     ```
   - Buscar veículo por placa:
     ```
     http://localhost:8080/api/vehicles/plate/ABC1234
     ```
   - Buscar reservas de uma OS:
     ```
     http://localhost:8080/api/stocks/reservations/service-order/b46ac51b-5ca6-439b-ba52-a36bd52e8648
     ```
2. Basta digitar o valor real no lugar do `{parâmetro}` no campo de URL

### 7. Como Adicionar Query Parameters

Para endpoints que usam parâmetros de consulta (query string), como `?minimumQuantity=10`:

1. Na request, vá até a aba **"Params"**
2. Na seção **"Query Params"**, adicione:
   - **Nome:** `minimumQuantity`
   - **Valor:** `10`
3. O Bruno montará automaticamente a URL completa:
   ```
   http://localhost:8080/api/stocks/product/1/minimum?minimumQuantity=10
   ```

> Alternativamente, você pode digitar os query params diretamente na URL, mas usar a aba "Params" é mais organizado e permite habilitar/desabilitar parâmetros individualmente.

### Exemplo Completo: Fluxo de Teste no Bruno

Aqui está a sequência recomendada para testar o sistema completo:

| # | Request | Método | URL |
|---|---|---|---|
| 1 | Login | `POST` | `http://localhost:8080/auth/login` |
| 2 | Criar Cliente | `POST` | `http://localhost:8080/api/clients` |
| 3 | Criar Veículo | `POST` | `http://localhost:8080/api/vehicles` |
| 4 | Criar Peça | `POST` | `http://localhost:8080/api/parts` |
| 5 | Criar Insumo | `POST` | `http://localhost:8080/api/supplies` |
| 6 | Criar Estoque (Peça) | `POST` | `http://localhost:8080/api/stocks` |
| 7 | Criar Estoque (Insumo) | `POST` | `http://localhost:8080/api/stocks` |
| 8 | Criar OS | `POST` | `http://localhost:8080/order` |
| 9 | Atualizar OS → EM_DIAGNOSTICO | `PATCH` | `http://localhost:8080/order/{id}` |
| 10 | Reservar Estoque | `POST` | `http://localhost:8080/api/stocks/reservations` |
| 11 | Atualizar OS → AGUARDANDO_APROVACAO | `PATCH` | `http://localhost:8080/order/{id}` |
| 12 | Consultar Orçamento | `GET` | `http://localhost:8080/api/budgets/service-order/{id}` |
| 13 | Atualizar OS → APROVADO | `PATCH` | `http://localhost:8080/order/{id}` |
| 14 | Listar Serviços da OS | `GET` | `http://localhost:8080/services/os/{id}` |
| 15 | Executar Serviços (DOING → DONE) | `PATCH` | `http://localhost:8080/services/update-status` |
| 16 | Atualizar OS → FINALIZADA | `PATCH` | `http://localhost:8080/order/{id}` |
| 17 | Atualizar OS → ENTREGUE | `PATCH` | `http://localhost:8080/order/{id}` |
| 18 | Monitoramento | `POST` | `http://localhost:8080/monitoring/all` |

---

## 📜 Scripts

O projeto inclui dois scripts shell que automatizam a inicialização do banco de dados e a validação do fluxo completo.

### `init.sh` — Inicialização do Banco de Dados

Este script é executado **automaticamente** pelo container PostgreSQL na primeira inicialização (via volume montado em `/docker-entrypoint-initdb.d/`).

**O que ele faz:**

1. **Cria todas as tabelas do sistema:**
   - `users`, `roles`, `groups`, `user_roles`, `user_groups` — Autenticação e autorização
   - `clients` — Cadastro de clientes
   - `vehicles` — Cadastro de veículos
   - `service_type` — Tipos de serviço disponíveis
   - `service` — Serviços vinculados às OS
   - `service_order` — Ordens de serviço
   - `products`, `parts`, `supplies` — Produtos (herança de tabela)
   - `stocks`, `stock_movements`, `stock_reservations` — Controle de estoque
   - `budgets`, `budget_items` — Orçamentos

2. **Insere dados de exemplo (mock data):**
   - Usuário super admin (`superadmin@system.com` / `coxinha123`)
   - 3 roles: `ROLE_ADMIN`, `ROLE_USER`, `ROLE_TECHNICIAN`
   - 3 clientes de exemplo
   - 3 veículos de exemplo
   - 6 tipos de serviço (TROCA_OLEO, ALINHAMENTO, BALANCEAMENTO, etc.)
   - 1 peça (Pastilha de Freio) e 1 insumo (Óleo 5W30)
   - 1 registro de estoque

> **Nota:** Se você precisar recriar o banco do zero, remova o volume do PostgreSQL e suba novamente:
> ```bash
> docker compose down -v
> docker compose up -d
> ```

### `validation.sh` — Validação End-to-End do Fluxo Completo

Este script simula o **ciclo de vida completo** de uma ordem de serviço, desde o login até a entrega do veículo, validando que todos os endpoints funcionam corretamente em sequência.

**Como executar:**

```bash
chmod +x validation.sh
./validation.sh
```

> A aplicação precisa estar rodando em `http://localhost:8080` antes de executar o script.

**Fluxo executado pelo script:**

| Etapa | Ação | Endpoint |
|---|---|---|
| 1 | Autenticação do super admin | `POST /auth/login` |
| 2 | Criação de uma nova OS | `POST /order` |
| 3 | Início do diagnóstico | `PATCH /order/{id}` → `EM_DIAGNOSTICO` |
| 4 | Consulta da OS | `GET /order/{id}` |
| 5 | Consulta de peças disponíveis | `GET /api/parts` |
| 6 | Consulta de estoque | `GET /api/stocks` |
| 7 | Reserva de estoque para a OS | `POST /api/stocks/reservations` |
| 8 | Aguardando aprovação do cliente | `PATCH /order/{id}` → `AGUARDANDO_APROVACAO` |
| 9 | Consulta do orçamento gerado | `GET /api/budgets/service-order/{id}` |
| 10 | Cliente aprova a OS | `PATCH /order/{id}` → `APROVADO` |
| 11 | Listagem dos serviços a executar | `GET /services/os/{id}` |
| 12-13 | Execução do 1º serviço (DOING → DONE) | `PATCH /services/update-status` |
| 14-15 | Execução do 2º serviço (DOING → DONE) | `PATCH /services/update-status` |
| 16 | Finalização da OS | `PATCH /order/{id}` → `FINALIZADA` |
| 17 | Entrega do veículo | `PATCH /order/{id}` → `ENTREGUE` |
| 18 | Monitoramento de tempo de execução | `POST /monitoring/all` |

O script inclui intervalos (`sleep`) entre as etapas para simular tempos reais de execução, permitindo que o monitoramento registre tempos significativos.

---

## 📄 Licença

Projeto acadêmico desenvolvido como Trabalho de Conclusão de Curso.

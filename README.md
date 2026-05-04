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

- [Documentação da API](#-documentação-da-api)
- [Visão Geral da Arquitetura](#-visão-geral-da-arquitetura)
- [Tecnologias](#-tecnologias)
- [Pré-requisitos e Como Executar](#-pré-requisitos-e-como-executar)
- [Referência Rápida de Endpoints](#-referência-rápida-de-endpoints)
- [Testando a API com Bruno](#-testando-a-api-com-bruno)
- [Scripts](#-scripts)

---

## 📚 Documentação da API

> **A documentação técnica completa da API — contratos de request/response, exemplos, esquemas e decisões de design — está centralizada no repositório de documentação do projeto:**
>
> ### 👉 [os-technical-documentation](https://github.com/TechChallenge-Software-Architetute/os-technical-documentation)

Além da documentação externa, a aplicação expõe o **Swagger UI** com a especificação OpenAPI gerada automaticamente a partir do código:

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

> O Swagger UI é a fonte mais atualizada dos contratos, pois reflete o código em execução em tempo real.

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
└── DataInitializer.java     # Inicializador de dados de teste
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
| **OWASP ZAP** | Stable | Análise dinâmica de segurança (DAST) |

### Banco de Dados

Optamos por um banco de dados **relacional (SQL)** — especificamente o **PostgreSQL** — ao invés de uma solução NoSQL. Essa escolha foi motivada pelas características do domínio da aplicação:

- **JOINs entre múltiplas entidades:** O sistema possui relacionamentos complexos entre clientes, veículos, ordens de serviço, serviços, produtos, estoque, reservas e orçamentos.
- **Integridade referencial:** Foreign keys garantem que não existam reservas de estoque para produtos inexistentes, nem ordens de serviço para clientes removidos.
- **Transações ACID:** Operações como a reserva atômica de estoque dependem de transações com garantias de atomicidade e isolamento.
- **Schema estruturado:** O domínio da oficina possui entidades bem definidas com campos fixos e tipos conhecidos.
- **Consultas complexas e agregações:** Funcionalidades como monitoramento de tempo médio de execução se beneficiam da expressividade do SQL.

---

## 🚀 Pré-requisitos e Como Executar

### Pré-requisitos

- **Java 21** (JDK) instalado
- **Docker** e **Docker Compose** instalados
- Porta **5432** (PostgreSQL), **8080** (aplicação) e **9000** (SonarQube) disponíveis

### Subir o stack completo com Docker

```bash
docker compose up --build
```

Isso irá iniciar, em ordem:

| Container | Serviço | Porta |
|---|---|---|
| `postgres_db` | PostgreSQL 16 | 5432 |
| `os_management` | Aplicação Spring Boot | 8080 |
| `os_management_validation` | Script de validação end-to-end | — |
| `os-management-sonarqube_db-1` | PostgreSQL para o SonarQube | — |
| `os-management-sonarqube-1` | SonarQube | 9000 |
| `zap_security_scan` | OWASP ZAP (DAST) | — |

> O `init.sh` é executado automaticamente pelo PostgreSQL na primeira inicialização, criando todas as tabelas e inserindo dados de exemplo.

### Credenciais padrão

| Campo | Valor |
|---|---|
| Email (API) | `superadmin@system.com` |
| Senha (API) | `coxinha123` |
| SonarQube | `admin` / `admin` |

### Executar a aplicação localmente (sem Docker)

**1. Suba apenas o banco:**
```bash
docker compose up postgres -d
```

**2. Execute a aplicação:**
```bash
./mvnw spring-boot:run
```

**3. Acesse:**

| Recurso | URL |
|---|---|
| API Base | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| SonarQube | http://localhost:9000 |

### Executar Testes

```bash
# Testes unitários + build
./mvnw clean package

# Testes unitários + integração (requer Docker)
./mvnw clean verify

# Apenas testes de integração
./mvnw failsafe:integration-test failsafe:verify

# Relatório de cobertura JaCoCo
./mvnw test jacoco:report
# → Relatório em: target/site/jacoco/index.html
```

### Análise com SonarQube

```bash
./mvnw sonar:sonar
```

> Certifique-se de que o SonarQube está rodando em `http://localhost:9000`.

### Varredura de Segurança com OWASP ZAP

```bash
# Com o stack já rodando, execute apenas o ZAP:
docker compose run --rm zap sh /zap/wrk/zap-scan.sh
```

Os relatórios são gerados em `./zap-reports/`:

| Arquivo | Conteúdo |
|---|---|
| `zap-report.html` | Relatório visual (abrir no navegador) |
| `zap-report.json` | Relatório em JSON (para CI/scripts) |
| `zap-scan.log` | Log completo da varredura |

---

## 📡 Referência Rápida de Endpoints

> Para contratos completos (schemas de request/response, exemplos e regras de negócio), consulte a documentação técnica:
> **[os-technical-documentation](https://github.com/TechChallenge-Software-Architetute/os-technical-documentation)**

**Autenticação:** Todos os endpoints (exceto `/auth/login` e `/signup`) exigem o header:
```
Authorization: Bearer <token>
```

### Autenticação

| Método | Endpoint | Descrição | Auth |
|---|---|---|---|
| `POST` | `/auth/login` | Realizar login e obter token JWT | ❌ |
| `POST` | `/signup` | Cadastrar novo usuário | ✅ ADMIN |

### Clientes

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/clients` | Cadastrar cliente |
| `GET` | `/api/clients` | Listar todos os clientes ativos |
| `GET` | `/api/clients/{id}` | Buscar cliente por ID |
| `GET` | `/api/clients/cpf/{cpf}` | Buscar cliente por CPF |
| `PUT` | `/api/clients/{id}` | Atualizar cliente |
| `DELETE` | `/api/clients/{id}` | Desativar cliente (soft delete) |

### Veículos

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/vehicles` | Cadastrar veículo |
| `GET` | `/api/vehicles/{id}` | Buscar veículo por ID |
| `GET` | `/api/vehicles/plate/{plate}` | Buscar veículo por placa |
| `GET` | `/api/vehicles/client/{clientId}` | Listar veículos de um cliente |
| `PUT` | `/api/vehicles/{id}` | Atualizar veículo |
| `DELETE` | `/api/vehicles/{id}` | Desativar veículo (soft delete) |

### Peças (Parts)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/parts` | Cadastrar peça |
| `GET` | `/api/parts` | Listar todas as peças ativas |
| `GET` | `/api/parts/{id}` | Buscar peça por ID |
| `GET` | `/api/parts/sku/{sku}` | Buscar peça por SKU |
| `PUT` | `/api/parts/{id}` | Atualizar peça |
| `DELETE` | `/api/parts/{id}` | Desativar peça (soft delete) |

### Insumos (Supplies)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/supplies` | Cadastrar insumo |
| `GET` | `/api/supplies` | Listar todos os insumos ativos |
| `GET` | `/api/supplies/{id}` | Buscar insumo por ID |
| `GET` | `/api/supplies/sku/{sku}` | Buscar insumo por SKU |
| `PUT` | `/api/supplies/{id}` | Atualizar insumo |
| `DELETE` | `/api/supplies/{id}` | Desativar insumo (soft delete) |

### Estoque (Stock)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/stocks` | Criar estoque para um produto |
| `GET` | `/api/stocks` | Listar todos os estoques |
| `GET` | `/api/stocks/low` | Listar estoques abaixo do mínimo |
| `GET` | `/api/stocks/product/{productId}` | Buscar estoque por produto |
| `PATCH` | `/api/stocks/product/{productId}/entry` | Registrar entrada de estoque |
| `PATCH` | `/api/stocks/product/{productId}/exit` | Registrar saída de estoque |
| `PATCH` | `/api/stocks/product/{productId}/minimum` | Atualizar quantidade mínima (`?minimumQuantity=X`) |
| `GET` | `/api/stocks/product/{productId}/movements` | Histórico de movimentações |

### Reservas de Estoque

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/stocks/reservations` | Reservar estoque para uma OS (atômico) |
| `PATCH` | `/api/stocks/reservations/service-order/{id}/confirm` | Confirmar reservas da OS |
| `PATCH` | `/api/stocks/reservations/service-order/{id}/release` | Liberar reservas da OS |
| `GET` | `/api/stocks/reservations/service-order/{id}` | Listar reservas de uma OS |

### Ordens de Serviço

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/order` | Criar ordem de serviço |
| `GET` | `/order` | Listar todas as OS |
| `GET` | `/order/{id}` | Buscar OS por ID (inclui orçamento) |
| `PATCH` | `/order/{id}` | Atualizar status da OS |

> **Status válidos (em ordem):** `RECEBIDA` → `EM_DIAGNOSTICO` → `AGUARDANDO_APROVACAO` → `APROVADO` → `FINALIZADA` → `ENTREGUE`

### Serviços

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/services` | Criar serviço |
| `GET` | `/services` | Listar todos os serviços |
| `GET` | `/services/{id}` | Buscar serviço por ID |
| `GET` | `/services/os/{idOS}` | Listar serviços de uma OS |
| `PATCH` | `/services/update-status` | Atualizar status do serviço |
| `GET` | `/service-types` | Listar tipos de serviço disponíveis |

> **Status do serviço:** `TO_DO` → `DOING` → `DONE`

### Orçamento

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/budgets/service-order/{serviceOrderId}` | Consultar orçamento da OS |

> O orçamento é **gerado automaticamente** quando a OS avança para `AGUARDANDO_APROVACAO`, com base nas reservas de estoque vinculadas. Os preços são congelados no momento da geração.

### Monitoramento

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/monitoring/all` | Tempo médio de execução por tipo de serviço |
| `GET` | `/monitoring/{serviceType}` | Tempo médio de um tipo específico |

---

## 🧪 Testando a API com Bruno

O [Bruno](https://www.usebruno.com/) é um cliente HTTP open-source para testar APIs. Para os contratos completos de cada endpoint (campos obrigatórios, tipos, exemplos de request/response), consulte:

> **[os-technical-documentation](https://github.com/TechChallenge-Software-Architetute/os-technical-documentation)**

### Configuração básica

1. **Instale o Bruno:** [https://www.usebruno.com/downloads](https://www.usebruno.com/downloads)
2. Crie uma collection **"OS Management API"**
3. Configure a URL base: `http://localhost:8080`

### Autenticação

Antes de qualquer request protegido, obtenha o token:

- **POST** `http://localhost:8080/auth/login`
- **Body (JSON):** `{ "email": "superadmin@system.com", "password": "coxinha123" }`
- **Copie o campo `token`** da resposta

Em cada request protegido, vá em **Auth → Bearer Token** e cole o token copiado.

> **Dica:** Configure o token no nível da collection (**botão direito na collection → Settings → Auth → Bearer Token**) para que todas as requests o herdem automaticamente.

### Sequência recomendada de testes

| # | Request | Método | Endpoint |
|---|---|---|---|
| 1 | Login | `POST` | `/auth/login` |
| 2 | Criar Cliente | `POST` | `/api/clients` |
| 3 | Criar Veículo | `POST` | `/api/vehicles` |
| 4 | Criar Peça | `POST` | `/api/parts` |
| 5 | Criar Insumo | `POST` | `/api/supplies` |
| 6 | Criar Estoque | `POST` | `/api/stocks` |
| 7 | Criar OS | `POST` | `/order` |
| 8 | OS → EM_DIAGNOSTICO | `PATCH` | `/order/{id}` |
| 9 | Reservar Estoque | `POST` | `/api/stocks/reservations` |
| 10 | OS → AGUARDANDO_APROVACAO | `PATCH` | `/order/{id}` |
| 11 | Consultar Orçamento | `GET` | `/api/budgets/service-order/{id}` |
| 12 | OS → APROVADO | `PATCH` | `/order/{id}` |
| 13 | Listar Serviços da OS | `GET` | `/services/os/{id}` |
| 14 | Executar Serviços (DOING → DONE) | `PATCH` | `/services/update-status` |
| 15 | OS → FINALIZADA | `PATCH` | `/order/{id}` |
| 16 | OS → ENTREGUE | `PATCH` | `/order/{id}` |
| 17 | Monitoramento | `POST` | `/monitoring/all` |

> Para os payloads exatos de cada etapa, consulte a **[documentação técnica](https://github.com/TechChallenge-Software-Architetute/os-technical-documentation)** ou o **[Swagger UI](http://localhost:8080/swagger-ui/index.html)** com a aplicação rodando.

---

## 📜 Scripts

### `init.sh` — Inicialização do Banco de Dados

Executado **automaticamente** pelo PostgreSQL na primeira inicialização via `/docker-entrypoint-initdb.d/`. Cria todas as tabelas do sistema e insere dados de exemplo:

- Usuário super admin (`superadmin@system.com` / `coxinha123`)
- 3 roles: `ROLE_ADMIN`, `ROLE_USER`, `ROLE_TECHNICIAN`
- 3 clientes e 3 veículos de exemplo
- 6 tipos de serviço (TROCA_OLEO, ALINHAMENTO, BALANCEAMENTO, etc.)
- 1 peça e 1 insumo com estoque inicial

> Para recriar o banco do zero:
> ```bash
> docker compose down -v
> docker compose up --build
> ```

### `validation.sh` — Validação End-to-End

Simula o ciclo de vida completo de uma OS (18 etapas), do login à entrega do veículo. Executado automaticamente pelo container `os_management_validation` após a aplicação subir.

Para executar manualmente (com a aplicação rodando):
```bash
chmod +x validation.sh
./validation.sh
```

| Etapa | Ação |
|---|---|
| 1 | Autenticação do super admin |
| 2 | Criação de OS |
| 3-4 | Diagnóstico e consulta |
| 5-7 | Consulta de peças, estoque e reserva |
| 8 | Aguardando aprovação |
| 9 | Consulta do orçamento |
| 10 | Aprovação pelo cliente |
| 11-15 | Execução dos serviços (DOING → DONE) |
| 16-17 | Finalização e entrega |
| 18 | Monitoramento de tempo de execução |

---

## 📄 Licença

Projeto acadêmico desenvolvido como Trabalho de Conclusão de Curso — FIAP Pós Tech.

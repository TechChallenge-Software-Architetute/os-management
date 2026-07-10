# OS Management — Sistema de Gestao de Ordens de Servico para Oficina Mecanica

Este projeto foi desenvolvido como parte do programa de **Pos-Graduacao da FIAP** (Pos Tech).

Uma oficina mecanica local vinha utilizando planilhas e anotacoes manuais para gerenciar suas operacoes do dia a dia. Com o tempo, essa abordagem gerou uma serie de problemas recorrentes:

- Dificuldade em priorizar atendimentos, causando atrasos e insatisfacao dos clientes
- Falta de controle confiavel sobre o estoque de pecas e insumos, gerando rupturas e compras excessivas
- Dificuldade em acompanhar o status em tempo real dos servicos em andamento em multiplos veiculos
- Perda do historico de clientes e veiculos, dificultando o atendimento em visitas recorrentes
- Ineficiencia no fluxo de orcamento e aprovacao, com calculos manuais e sem registro do valor no momento da aprovacao
- Ausencia de notificacao ao cliente sobre o andamento do seu veiculo

Para resolver esses problemas, desenvolvemos o **OS Management** — um sistema completo de gestao de ordens de servico projetado para digitalizar e otimizar as operacoes da oficina. O sistema cobre todo o ciclo de vida de uma ordem de servico, desde o recebimento ate a entrega do veiculo.

**Principais funcionalidades:**

- Cadastro de clientes (CPF e CNPJ) e veiculos com CRUD completo e soft delete
- Catalogo de pecas e insumos com busca por SKU e controle de estoque
- Gestao de estoque com rastreamento de entradas/saidas, alertas de estoque minimo e historico de movimentacoes
- Reservas atomicas de estoque vinculadas a ordens de servico, evitando alocacao duplicada
- Geracao automatica de orcamento baseado nos produtos reservados, com snapshot de precos congelado no momento da aprovacao
- Endpoint unificado de decisao do cliente (aprovacao ou recusa do orcamento)
- Liberacao automatica de reservas de estoque ao recusar um orcamento
- Gestao do ciclo de vida da OS com maquina de estados validada no dominio
- Status RECUSADA com possibilidade de reabertura da OS
- Listagem de OS com ordenacao por prioridade de status e filtro de finalizadas/entregues
- Busca de historico completo de OS por CPF/CNPJ do cliente
- Notificacao por email (AWS SES) a cada mudanca de status da OS
- Acompanhamento individual do status de cada servico dentro da OS (a fazer, em andamento, concluido)
- Monitoramento de tempo de execucao dos servicos para analise de desempenho
- Autenticacao via JWT e controle de acesso baseado em roles
- Container Docker seguro com usuario nao-root
- Cobertura de testes minima de 90% (JaCoCo)

> **Projeto de Pos-Graduacao** — FIAP Pos Tech

---

## Sumario

- [Documentacao da API](#documentacao-da-api)
- [Visao Geral da Arquitetura](#visao-geral-da-arquitetura)
- [Notificacao por Email (AWS SES)](#notificacao-por-email-aws-ses)
- [Tecnologias](#tecnologias)
- [Pre-requisitos e Como Executar](#pre-requisitos-e-como-executar)
- [Referencia Rapida de Endpoints](#referencia-rapida-de-endpoints)
- [Testando a API com Bruno](#testando-a-api-com-bruno)
- [Scripts](#scripts)
- [Testes](#testes)

---

## Documentacao da API

A aplicacao expoe o **Swagger UI** com a especificacao OpenAPI gerada automaticamente a partir do codigo:

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

---

## Visao Geral da Arquitetura

O projeto adota a **Arquitetura Hexagonal (Ports & Adapters)**, onde cada modulo possui separacao clara entre dominio, aplicacao e infraestrutura. O dominio nao possui dependencias de frameworks externos.

### Estrutura de Diretorios

```
src/main/java/com/os/workshop/
├── adapter/
│   ├── in/web/                     # Controllers REST (entrada HTTP)
│   │   ├── budget/
│   │   ├── client/
│   │   ├── monitoring/
│   │   ├── product/ (part + supply)
│   │   ├── service/
│   │   ├── serviceorder/
│   │   ├── stock/
│   │   ├── user/
│   │   └── vehicle/
│   └── out/persistence/            # Adapters de persistencia (implementam ports)
├── application/                    # Use Cases + Port interfaces
│   ├── budget/
│   ├── client/
│   ├── monitoring/
│   ├── notification/               # Notification service + EmailNotificationPort
│   ├── product/ (part + supply)
│   ├── service/
│   ├── serviceorder/
│   ├── stock/
│   ├── user/
│   └── vehicle/
├── domain/                         # Entidades de dominio puras (sem dependencias externas)
│   ├── budget/
│   ├── client/                     # Client, Document (CPF/CNPJ), Cpf, Cnpj
│   ├── monitoring/
│   ├── product/
│   ├── service/
│   ├── serviceorder/               # ServiceOrder (rich domain), state machine, Decision
│   ├── stock/
│   ├── user/
│   └── vehicle/
├── infrastructure/                 # Configuracoes, JPA entities, Security, SES
│   ├── config/
│   ├── notification/               # SesEmailService, EmailTemplateRenderer, SesConfig
│   ├── persistence/
│   └── security/
└── WorkshopApplication.java
```

### Fluxo Principal da Ordem de Servico (Maquina de Estados)

```
RECEBIDA → EM_DIAGNOSTICO → AGUARDANDO_APROVACAO → APROVADO → EM_EXECUCAO → FINALIZADA → ENTREGUE
                                     |
                                     └→ RECUSADA → (reabertura) → EM_DIAGNOSTICO
```

| Status | Descricao |
|--------|-----------|
| RECEBIDA | OS criada com servicos solicitados |
| EM_DIAGNOSTICO | Mecanico avalia o veiculo e reserva pecas/insumos |
| AGUARDANDO_APROVACAO | Orcamento gerado automaticamente, aguarda decisao do cliente |
| APROVADO | Cliente aprovou, mecanico inicia execucao |
| RECUSADA | Cliente recusou, reservas de estoque liberadas automaticamente |
| EM_EXECUCAO | Servicos sendo realizados |
| FINALIZADA | Todos os servicos concluidos |
| ENTREGUE | Veiculo devolvido ao cliente |

A validacao de transicoes e feita no dominio (ServiceOrder). Transicoes invalidas retornam HTTP 422.

---

## Notificacao por Email (AWS SNS)

### Por que AWS SNS?

Escolhemos o **Amazon Simple Notification Service (SNS)** como solucao de notificacao pelos seguintes motivos:

1. **Disponibilidade no AWS Academy**: O SES nao esta disponivel no Lab do Academy, mas o SNS sim. SNS suporta protocolo email como subscriber, permitindo enviar notificacoes para emails cadastrados.

2. **Simplicidade**: Um topico SNS com subscribers de email funciona sem infraestrutura adicional. A aplicacao publica a mensagem e o SNS entrega.

3. **Custo**: SNS oferece 1 milhao de publicacoes gratuitas/mes no free tier.

4. **Extensibilidade**: O mesmo topico pode ter subscribers de outros tipos no futuro (SMS, Lambda, SQS, HTTP) sem alterar o codigo da aplicacao.

5. **Desacoplamento**: A aplicacao nao precisa conhecer os destinatarios — apenas publica no topico. O SNS gerencia a entrega.

### Como funciona

```
Mudanca de Status
       |
       v
OrderStatusNotificationService
       |
       +-- Busca Client por documento -> nome
       +-- Busca Vehicle por placa -> marca/modelo
       |
       v
EmailNotificationPort (interface)
       |
       v
SnsNotificationService (@Async)
       |
       +-- Monta mensagem em texto formatado
       +-- Publica no topico SNS via AWS SDK v2
       +-- SNS entrega para todos os subscribers (email)
```

**Caracteristicas:**

- **Assincrono**: O envio e feito com `@Async`, nao bloqueando a thread principal
- **Best-effort**: Falha na publicacao e registrada em log mas nao reverte a operacao de negocio
- **Mensagem formatada**: Texto com saudacao personalizada, status, veiculo e mensagem contextual
- **Port & Adapter**: A interface `EmailNotificationPort` permite trocar o provider sem alterar logica de negocio

### Configuracao

Variaveis de ambiente necessarias:

| Variavel | Descricao | Default |
|----------|-----------|---------|
| `AWS_REGION` | Regiao AWS do SNS | us-east-1 |
| `AWS_SNS_TOPIC_ARN` | ARN do topico SNS | — |
| `AWS_ACCESS_KEY_ID` | Credencial AWS | — |
| `AWS_SECRET_ACCESS_KEY` | Credencial AWS | — |
| `AWS_SESSION_TOKEN` | Token de sessao (AWS Academy) | — |

### Configuracao via arquivo `.env`

O projeto usa um arquivo `.env` na raiz para armazenar as credenciais AWS. Este arquivo esta no `.gitignore` e nunca deve ser commitado.

```env
AWS_REGION=us-east-1
AWS_SNS_TOPIC_ARN=arn:aws:sns:us-east-1:123456789012:os-management-notifications
AWS_ACCESS_KEY_ID=COLE_AQUI
AWS_SECRET_ACCESS_KEY=COLE_AQUI
AWS_SESSION_TOKEN=COLE_AQUI
```

O `docker-compose.yml` le automaticamente o `.env`. Basta atualizar as credenciais e subir os containers.

**Como atualizar as credenciais (a cada sessao do Lab):**

1. Acesse o AWS Academy e inicie o Lab
2. Clique em **AWS Details** > **Show** (ao lado de AWS CLI)
3. Copie `aws_access_key_id`, `aws_secret_access_key` e `aws_session_token`
4. Cole no arquivo `.env` substituindo os valores anteriores
5. Suba a aplicacao: `docker compose up --build`

> As credenciais do AWS Academy expiram a cada sessao do Lab (~4h). Toda vez que reiniciar o Lab, atualize o `.env` com as novas credenciais.

### Setup no AWS Academy — Criar Topico SNS

1. Acesse o console AWS: **SNS > Topics**
2. Clique em **Create topic**
3. Tipo: **Standard**
4. Nome: `os-management-notifications`
5. Clique em **Create topic**
6. Copie o **ARN** do topico (ex: `arn:aws:sns:us-east-1:837687730031:os-management-notifications`)
7. Cole o ARN no `.env` em `AWS_SNS_TOPIC_ARN`

### Setup no AWS Academy — Adicionar Subscriber de Email

1. No topico criado, clique em **Create subscription**
2. Protocolo: **Email**
3. Endpoint: digite seu email (ex: `amanda.lcosta33@gmail.com`)
4. Clique em **Create subscription**
5. Acesse sua caixa de entrada e clique em **Confirm subscription** no email da AWS
6. Status muda para **Confirmed**

> Cada email que quiser receber notificacoes precisa ser adicionado como subscriber e confirmado. Para demonstracao, use seu proprio email.

---

## Tecnologias

| Tecnologia | Versao | Finalidade |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 4.0.5 | Framework web e DI |
| Spring Security | — | Autenticacao e autorizacao |
| Spring Data JPA | — | Persistencia de dados |
| PostgreSQL | 16 | Banco de dados relacional |
| Docker / Docker Compose | — | Containerizacao e orquestracao |
| Maven | — | Gerenciamento de dependencias e build |
| AWS SNS (SDK v2) | 2.29.1 | Notificacao por email via topico |
| MapStruct | 1.6.3 | Mapeamento entre DTOs e entidades |
| Lombok | — | Reducao de boilerplate |
| JWT (jjwt) | 0.13.0 | Tokens de autenticacao |
| SpringDoc OpenAPI | 3.0.2 | Documentacao Swagger UI |
| JaCoCo | 0.8.14 | Cobertura de testes (minimo 90%) |
| Testcontainers | 1.20.4 | Testes de integracao com PostgreSQL real |
| OWASP ZAP | — | Varredura de seguranca DAST |
| SonarQube | Community | Analise estatica de codigo |

---

## Pre-requisitos e Como Executar

### Pre-requisitos

- Java 21 (JDK) instalado
- Docker e Docker Compose instalados
- Porta 5432 (PostgreSQL), 8080 (aplicacao) e 9000 (SonarQube) disponiveis

### Subir o stack completo com Docker

```bash
docker compose up --build
```

Containers iniciados:

| Container | Servico | Porta |
|---|---|---|
| postgres_db | PostgreSQL 16 | 5432 |
| os_management | Aplicacao Spring Boot | 8080 |
| validation | Script de validacao E2E | — |

### Credenciais padrao

| Campo | Valor |
|---|---|
| Email (API) | superadmin@system.com |
| Senha (API) | coxinha123 |

### Executar localmente (sem Docker)

```bash
docker compose up postgres -d
./mvnw spring-boot:run
```

### Profiles adicionais

```bash
docker compose --profile security up    # OWASP ZAP
docker compose --profile quality up     # SonarQube
```

---

## Referencia Rapida de Endpoints

**Autenticacao:** Todos os endpoints (exceto /auth/login) exigem header `Authorization: Bearer <token>`

### Autenticacao

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /auth/login | Login e obter token JWT |
| POST | /signup | Cadastrar usuario (ADMIN) |

### Clientes

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/clients | Cadastrar cliente (CPF ou CNPJ) |
| GET | /api/clients | Listar clientes ativos |
| GET | /api/clients/{id} | Buscar por ID |
| GET | /api/clients/cpf/{document} | Buscar por CPF ou CNPJ |
| PUT | /api/clients/{id} | Atualizar cliente |
| DELETE | /api/clients/{id} | Desativar (soft delete) |
| GET | /api/clients/my-orders | Listar OS do cliente autenticado |
| GET | /api/clients/my-orders/{orderId} | Detalhe de uma OS |
| POST | /api/clients/my-orders/{orderId}/decision | Aprovar ou recusar orcamento |

**Payload de decisao:**
```json
{
  "decision": "APPROVED",
  "reason": null
}
```
```json
{
  "decision": "REJECTED",
  "reason": "Preco acima do esperado"
}
```

### Veiculos

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/vehicles | Cadastrar veiculo |
| GET | /api/vehicles/{id} | Buscar por ID |
| GET | /api/vehicles/plate/{plate} | Buscar por placa |
| GET | /api/vehicles/client/{clientId} | Listar veiculos de um cliente |
| PUT | /api/vehicles/{id} | Atualizar |
| DELETE | /api/vehicles/{id} | Desativar (soft delete) |

### Pecas (Parts)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/parts | Cadastrar peca |
| GET | /api/parts | Listar ativas |
| GET | /api/parts/{id} | Buscar por ID |
| GET | /api/parts/sku/{sku} | Buscar por SKU |
| PUT | /api/parts/{id} | Atualizar |
| DELETE | /api/parts/{id} | Desativar |

### Insumos (Supplies)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/supplies | Cadastrar insumo |
| GET | /api/supplies | Listar ativos |
| GET | /api/supplies/{id} | Buscar por ID |
| GET | /api/supplies/sku/{sku} | Buscar por SKU |
| PUT | /api/supplies/{id} | Atualizar |
| DELETE | /api/supplies/{id} | Desativar |

### Estoque (Stock)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/stocks | Criar estoque para produto |
| GET | /api/stocks | Listar todos |
| GET | /api/stocks/low | Listar abaixo do minimo |
| GET | /api/stocks/product/{productId} | Buscar por produto |
| PATCH | /api/stocks/product/{productId}/entry | Entrada de estoque |
| PATCH | /api/stocks/product/{productId}/exit | Saida de estoque |
| PATCH | /api/stocks/product/{productId}/minimum | Atualizar minimo |
| GET | /api/stocks/product/{productId}/movements | Historico movimentacoes |

### Reservas de Estoque

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/stocks/reservations | Reservar para OS |
| PATCH | /api/stocks/reservations/service-order/{id}/confirm | Confirmar reservas |
| PATCH | /api/stocks/reservations/service-order/{id}/release | Liberar reservas |
| GET | /api/stocks/reservations/service-order/{id} | Listar reservas da OS |

### Ordens de Servico

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /order | Criar OS |
| GET | /order | Listar OS ativas (ordenada por prioridade, exclui finalizadas/entregues) |
| GET | /order/id/{id} | Buscar OS por ID |
| GET | /order/document/{cpfCnpj} | Historico completo de OS por CPF/CNPJ |
| PATCH | /order/{id} | Atualizar status (204 No Content) |

**Ordenacao da listagem (GET /order):**
1. EM_EXECUCAO (maior prioridade)
2. AGUARDANDO_APROVACAO
3. EM_DIAGNOSTICO
4. RECEBIDA
5. APROVADO
6. RECUSADA

Dentro do mesmo status: mais antigas primeiro (createdAt ASC).

**Transicoes de status validas:**

| De | Para |
|----|------|
| RECEBIDA | EM_DIAGNOSTICO |
| EM_DIAGNOSTICO | AGUARDANDO_APROVACAO |
| AGUARDANDO_APROVACAO | APROVADO, RECUSADA |
| APROVADO | EM_EXECUCAO |
| EM_EXECUCAO | FINALIZADA |
| FINALIZADA | ENTREGUE |
| RECUSADA | EM_DIAGNOSTICO |

### Servicos

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /services | Criar servico |
| GET | /services | Listar todos |
| GET | /services/{id} | Buscar por ID |
| GET | /services/os/{idOS} | Listar servicos de uma OS |
| PATCH | /services/update-status | Atualizar status do servico |
| GET | /service-types | Listar tipos disponiveis |

### Orcamento

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | /api/budgets/service-order/{serviceOrderId} | Consultar orcamento da OS |

### Monitoramento

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /monitoring/all | Tempo medio por tipo de servico |
| GET | /monitoring/{serviceType} | Tempo medio de um tipo |

---

## Testando a API com Bruno

O [Bruno](https://www.usebruno.com/) e um cliente HTTP open-source para testar APIs.

1. Instale o Bruno: https://www.usebruno.com/downloads
2. Abra a collection em `bruno/os-management-api/`
3. Configure a URL base: http://localhost:8080

### Sequencia recomendada

| # | Request | Metodo | Endpoint |
|---|---|---|---|
| 1 | Login | POST | /auth/login |
| 2 | Criar Cliente | POST | /api/clients |
| 3 | Criar Veiculo | POST | /api/vehicles |
| 4 | Criar Peca | POST | /api/parts |
| 5 | Criar Estoque | POST | /api/stocks |
| 6 | Criar OS | POST | /order |
| 7 | EM_DIAGNOSTICO | PATCH | /order/{id} |
| 8 | Reservar Estoque | POST | /api/stocks/reservations |
| 9 | AGUARDANDO_APROVACAO | PATCH | /order/{id} |
| 10 | Consultar Orcamento | GET | /api/budgets/service-order/{id} |
| 11 | Aprovar (Decision) | POST | /api/clients/my-orders/{id}/decision |
| 12 | EM_EXECUCAO | PATCH | /order/{id} |
| 13 | Executar Servicos | PATCH | /services/update-status |
| 14 | FINALIZADA | PATCH | /order/{id} |
| 15 | ENTREGUE | PATCH | /order/{id} |
| 16 | Monitoramento | POST | /monitoring/all |

---

## Scripts

### init.sh

Executado automaticamente pelo PostgreSQL na primeira inicializacao. Cria todas as tabelas e insere dados de exemplo.

### validation.sh

Simula o ciclo de vida completo de uma OS (25 etapas), incluindo:
- Autenticacao
- Criacao de OS
- Diagnostico e reserva de estoque
- Aprovacao via endpoint de decisao
- Execucao dos servicos
- Finalizacao e entrega
- Monitoramento
- Listagem ordenada e busca por documento

Para executar manualmente:
```bash
chmod +x validation.sh
./validation.sh
```

---

## Testes

### Executar testes

```bash
# Testes unitarios
./mvnw clean test

# Testes unitarios + integracao + cobertura
./mvnw clean verify

# Apenas integracao
./mvnw verify -Dsurefire.skip=true

# Relatorio de cobertura
./mvnw test jacoco:report
# Relatorio em: target/site/jacoco/index.html
```

### Cobertura minima

O build falha se a cobertura de linha (JaCoCo) ficar abaixo de **90%**. A verificacao e feita automaticamente na fase `verify`.

### Testes de integracao

Utilizam Testcontainers com PostgreSQL 16 real em container Docker. Requerem Docker Desktop rodando.

Cenarios cobertos:
- Autenticacao (signup, login, acesso protegido)
- CRUD de clientes e veiculos
- Fluxo completo de OS (criacao, transicoes, finalizacao)
- Decisao do cliente (aprovacao e recusa via endpoint)
- Validacao de ownership (403)
- Transicao invalida (422)
- Busca por documento
- Estoque (entrada, saida, reserva)

---

## Licenca

Projeto academico desenvolvido como Trabalho de Conclusao de Curso — FIAP Pos Tech.

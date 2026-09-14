# Desenho da Arquitetura — OS Management

> Ver também: [RFCs](rfc/README.md) (decisões técnicas avaliadas) e
> [ADRs](adr/README.md) (decisões arquiteturais permanentes).

## Visão Consolidada da Solução (Multi-Repositório)

O sistema é composto por 5 repositórios com ciclos de deploy independentes
(ver [ADR-0003](adr/ADR-0003-split-multiplos-repositorios.md)). O diagrama
abaixo mostra a visão de nuvem completa — API Gateway, funções serverless de
autenticação, cluster Kubernetes, banco gerenciado e observabilidade — algo
que os diagramas C4 da seção seguinte não cobrem sozinhos, pois eles têm
zoom apenas no container da aplicação principal.

```mermaid
flowchart TB
    cliente([Cliente])
    staff([Mecânico / Admin])

    subgraph GW["os-management-gateway"]
        apigw[AWS API Gateway REST]
    end

    subgraph LAMBDA["os-management-lambda"]
        issuer[Lambda: Auth Issuer]
        authz[Lambda: Token Authorizer]
    end

    subgraph K8S["os-management-k8s-terraform + os-management"]
        eks[EKS Cluster os-management-env]
        app[Pods: API Spring Boot<br/>HPA min1/max6]
        dd_agent[Datadog Agent<br/>APM + logs + kube-state-metrics]
        eks --> app
        app -. sidecar/initContainer .-> dd_agent
    end

    subgraph DB["os-management-database"]
        rds[(RDS PostgreSQL 16)]
    end

    subgraph OBS["Datadog SaaS"]
        dash[Dashboards + Monitores + Alertas]
    end

    cliente -- "POST /auth {cpf}" --> apigw
    apigw --> issuer
    issuer -- "SELECT clients WHERE document" --> rds
    issuer -- "JWT" --> cliente

    cliente -- "ANY /* Bearer JWT" --> apigw
    apigw --> authz
    authz -- Allow/Deny --> apigw
    apigw -- "HTTP_PROXY" --> app

    staff -- "POST /auth/login + Bearer JWT" --> app
    app -- "JDBC" --> rds
    app -- "publica notificação" --> sns[(AWS SNS)]
    sns --> emailCliente([E-mail do Cliente])

    dd_agent --> dash
```

#### Descrição dos Componentes por Repositório

| Repositório | Componente no diagrama | Papel na solução |
|---|---|---|
| `os-management-gateway` | AWS API Gateway | Porta de entrada pública para o cliente final; roteia `/auth` ao issuer e `/*` ao backend via authorizer |
| `os-management-lambda` | Lambda Auth Issuer + Token Authorizer | Emite e valida o JWT de CPF (ver [RFC-0003](rfc/RFC-0003-estrategia-autenticacao.md)) |
| `os-management-k8s-terraform` | EKS Cluster + metrics-server | Cluster gerenciado onde a aplicação roda; base para o HPA (ver [ADR-0002](adr/ADR-0002-uso-hpa.md)) |
| `os-management` | Pods da API + manifests K8s | Lógica de negócio, autenticação de staff, orquestração dos fluxos de OS |
| `os-management-database` | RDS PostgreSQL | Banco gerenciado único, consumido pela API e pela Lambda issuer (ver [RFC-0002](rfc/RFC-0002-escolha-banco-dados.md) e o [diagrama ER](../../os-management-database/README.md#modelagem-do-banco-de-dados)) |
| — | Datadog Agent + SaaS | Observabilidade: latência de API, CPU/memória dos pods, healthchecks/uptime, logs estruturados correlacionados, métricas de negócio (ver `os-management/datadog/README.md`) |

---

## C4 Model

### Nivel 1 — Context Diagram (Diagrama de Contexto)

Mostra o sistema OS Management e suas interacoes com atores externos.

```mermaid
C4Context
    title OS Management - Diagrama de Contexto (C4 Level 1)

    Person(cliente, "Cliente", "Dono do veiculo. Aprova/recusa orcamentos via app.")
    Person(mecanico, "Mecanico/Tecnico", "Realiza diagnostico, executa servicos, gerencia estoque.")
    Person(admin, "Administrador", "Gerencia usuarios, clientes, catalogos e configuracoes.")

    System(osManagement, "OS Management", "Sistema de gestao de ordens de servico para oficina mecanica. Gerencia todo o ciclo de vida da OS.")

    System_Ext(sns, "AWS SNS", "Servico de notificacao por email via topico.")
    SystemDb_Ext(postgres, "PostgreSQL 16", "Banco de dados relacional para persistencia.")

    Rel(cliente, osManagement, "Aprova/recusa orcamento, consulta suas OS", "HTTPS/JSON")
    Rel(mecanico, osManagement, "Cria OS, reserva estoque, atualiza status", "HTTPS/JSON")
    Rel(admin, osManagement, "Gerencia usuarios, clientes, catalogos", "HTTPS/JSON")
    Rel(osManagement, sns, "Publica notificacoes de status", "HTTPS/AWS SDK")
    Rel(osManagement, postgres, "Persiste dados", "JDBC/TLS")
```

#### Descricao dos Elementos

| Elemento | Tipo | Descricao |
|----------|------|-----------|
| Cliente | Pessoa | Dono do veiculo. Recebe notificacoes por email. Aprova ou recusa orcamentos via endpoint autenticado. |
| Mecanico/Tecnico | Pessoa | Profissional da oficina. Cria OS, realiza diagnostico, reserva pecas, executa servicos, atualiza status. |
| Administrador | Pessoa | Gerencia usuarios, clientes, veiculos, catalogos de pecas/insumos e configuracoes do sistema. |
| OS Management | Sistema | Aplicacao principal. API REST Spring Boot que gerencia todo o ciclo de vida da OS. |
| AWS SES | Sistema Externo | Servico da AWS para envio de emails HTML com notificacoes de mudanca de status. |
| PostgreSQL 16 | Banco de Dados | Armazena clientes, veiculos, OS, servicos, estoque, orcamentos, usuarios. |

---

### Nivel 2 — Container Diagram (Diagrama de Containers)

Mostra os containers (unidades de deploy) que compoem o sistema e suas interacoes.

```mermaid
C4Container
    title OS Management - Diagrama de Containers (C4 Level 2)

    Person(cliente, "Cliente", "Aprova/recusa orcamentos")
    Person(mecanico, "Mecanico", "Opera o sistema")

    System_Boundary(system, "OS Management System") {
        Container(api, "API Application", "Java 21, Spring Boot 4.0.5", "API REST que implementa toda a logica de negocio da oficina. Arquitetura Hexagonal.")
        ContainerDb(db, "Database", "PostgreSQL 16", "Armazena todos os dados: clientes, veiculos, OS, servicos, estoque, orcamentos, usuarios.")
    }

    System_Ext(sns, "AWS SNS", "Notificacao por email")

    Rel(cliente, api, "POST /api/clients/my-orders/{id}/decision", "HTTPS/JSON + JWT")
    Rel(mecanico, api, "CRUD endpoints + PATCH /order/{id}", "HTTPS/JSON + JWT")
    Rel(api, db, "Leitura/Escrita", "JDBC PostgreSQL")
    Rel(api, sns, "Publica notificacoes", "AWS SDK v2")
```

#### Descricao dos Containers

| Container | Tecnologia | Responsabilidade |
|-----------|-----------|-----------------|
| API Application | Java 21, Spring Boot 4.0.5, Docker (non-root) | Toda a logica de negocio. Autenticacao JWT. Validacao. Orquestracao de fluxos. |
| Database | PostgreSQL 16, Docker | Persistencia relacional. Integridade referencial. Transacoes ACID. |
| AWS SNS | Servico gerenciado AWS | Publicacao de notificacoes de status para subscribers (email). |

---

## Componentes da Aplicacao (dentro do Container API)

### Arquitetura Hexagonal — Camadas

```
+------------------------------------------------------------------+
|                      ADAPTER IN (Web/REST)                        |
|                                                                  |
|  Controllers: Client, ServiceOrder, Vehicle, Part, Supply,       |
|  Stock, Service, Budget, Monitoring, User                        |
|  DTOs: Request/Response records                                  |
+------------------------------------------------------------------+
                              |
                              v
+------------------------------------------------------------------+
|                    APPLICATION (Use Cases)                        |
|                                                                  |
|  Use Cases: CreateOrder, UpdateOrder, DecideOrder, ListOrders,   |
|  FindOrdersByDocument, CreateClient, ReserveStock, etc.          |
|  Event Listeners: BudgetEventListener, StockRejectionListener    |
|  Notification: OrderStatusNotificationService                    |
|  Port Interfaces (OUT): ClientRepository, ServiceOrderRepository,|
|  StockRepository, VehicleRepository, EmailNotificationPort       |
+------------------------------------------------------------------+
                              |
                              v
+------------------------------------------------------------------+
|                         DOMAIN                                   |
|                                                                  |
|  Entities: ServiceOrder (rich), Client, Vehicle, Stock, Budget   |
|  Value Objects: Document (CPF/CNPJ), LicensePlate                |
|  Enums: OrderServiceStatusEnum, Decision, DocumentType           |
|  Events: OrderRejectedEvent, ReservationChangedEvent             |
|  Exceptions: InvalidStatusTransitionException, ClientNotFound    |
+------------------------------------------------------------------+
                              |
                              v
+------------------------------------------------------------------+
|                    ADAPTER OUT (Persistence)                      |
|                                                                  |
|  Persistence Adapters: ClientPersistenceAdapter,                 |
|  ServiceOrderPersistenceAdapter, StockPersistenceAdapter, etc.   |
|  Mappers: ClientMapper, VehicleMapper, StockMapper (MapStruct)   |
+------------------------------------------------------------------+
                              |
                              v
+------------------------------------------------------------------+
|                     INFRASTRUCTURE                                |
|                                                                  |
|  JPA Entities: ClientEntity, ServiceOrderEntity, StockEntity...  |
|  Security: JwtFilter, JwtService, SecurityConfig                 |
|  Notification: SesEmailService, EmailTemplateRenderer, SesConfig |
|  Config: GlobalExceptionHandler, AsyncConfig                     |
+------------------------------------------------------------------+
```

### Modulos de Dominio

| Modulo | Responsabilidade | Entidades Principais |
|--------|-----------------|---------------------|
| ServiceOrder | Ciclo de vida da OS com maquina de estados | ServiceOrder, OrderServiceStatusEnum, Decision |
| Client | Cadastro de clientes com CPF/CNPJ | Client, Document, Cpf, Cnpj |
| Vehicle | Cadastro de veiculos | Vehicle, LicensePlate, VehicleType |
| Product | Catalogo de pecas e insumos | Product (abstract), Part, Supply |
| Stock | Controle de estoque e reservas | Stock, StockMovement, StockReservation |
| Budget | Orcamento automatico | Budget, BudgetItem |
| Service | Servicos dentro da OS | WorkshopService, ServiceType, Status |
| Monitoring | Metricas de performance | ServiceAverageTime, AverageTimeEnum |
| User | Autenticacao e autorizacao | User, Role, Group |
| Notification | Envio de emails | EmailNotificationPort, SesEmailService |

### Fluxo Principal — Ciclo de Vida da OS

```mermaid
stateDiagram-v2
    [*] --> RECEBIDA : Criar OS
    RECEBIDA --> EM_DIAGNOSTICO : Mecanico inicia avaliacao
    EM_DIAGNOSTICO --> AGUARDANDO_APROVACAO : Diagnostico concluido
    AGUARDANDO_APROVACAO --> APROVADO : Cliente aprova
    AGUARDANDO_APROVACAO --> RECUSADA : Cliente recusa
    APROVADO --> EM_EXECUCAO : Mecanico inicia servicos
    EM_EXECUCAO --> FINALIZADA : Todos servicos concluidos
    FINALIZADA --> ENTREGUE : Veiculo devolvido
    RECUSADA --> EM_DIAGNOSTICO : OS reaberta
    ENTREGUE --> [*]
```

### Comunicacao entre Componentes

| Origem | Destino | Mecanismo | Descricao |
|--------|---------|-----------|-----------|
| DecideOrderUseCase | StockRejectionEventListener | Spring Event (sincrono) | Ao recusar, publica OrderRejectedEvent |
| StockRejectionEventListener | ReleaseReservationUseCase | Chamada direta | Libera reservas ACTIVE da OS |
| ReserveStockUseCase | BudgetEventListener | Spring Event (sincrono) | Ao reservar, publica ReservationChangedEvent |
| BudgetEventListener | RecalculateBudgetUseCase | Chamada direta | Recalcula orcamento |
| UpdateOrderUseCase | OrderStatusNotificationService | Chamada direta | Notifica cliente |
| OrderStatusNotificationService | SesEmailService | Via port interface (@Async) | Envia email de forma assincrona |

### Seguranca

| Aspecto | Implementacao |
|---------|--------------|
| Autenticacao | JWT stateless (Bearer token) |
| Autorizacao | Role-based (ADMIN, TECHNICIAN, USER) |
| Ownership | Email JWT -> Client -> Document == OS.cpfCnpj |
| Container | Usuario non-root no Dockerfile |
| Endpoints publicos | /auth/login, /swagger-ui/**, /v3/api-docs/** |
| Endpoints cliente | /api/clients/my-orders/** (ROLE_USER) |
| Demais endpoints | ROLE_ADMIN ou ROLE_TECHNICIAN |

---

## Diagramas de Sequência

### Autenticação via CPF

Fluxo completo de emissão e validação do JWT de cliente (API Gateway →
Lambda issuer/authorizer → backend). Diagrama detalhado e mantido como fonte
única em [`os-management-lambda/README.md § Authentication sequence`](../../os-management-lambda/README.md#authentication-sequence);
resumo abaixo para referência rápida:

```mermaid
sequenceDiagram
    participant C as Cliente
    participant G as API Gateway
    participant I as Lambda: Auth Issuer
    participant D as RDS PostgreSQL
    participant Z as Lambda: Token Authorizer
    participant B as Backend (EKS)

    C->>G: POST /auth { cpf }
    G->>I: proxy event
    I->>I: valida dígitos verificadores do CPF
    I->>D: SELECT id, name, active WHERE document = cpf
    alt não encontrado
        I-->>C: 404 Client not found
    else inativo
        I-->>C: 403 Client is inactive
    else válido e ativo
        I-->>C: 200 { token JWT, expiresIn, client }
    end

    C->>G: ANY /order (Authorization: Bearer JWT)
    G->>Z: TOKEN authorizer
    Z->>Z: verifica assinatura + expiração (HS256)
    alt válido
        Z-->>G: Allow (principal = CPF)
        G->>B: encaminha requisição
        B-->>C: 200 resposta protegida
    else inválido/expirado
        Z-->>G: 401 Unauthorized
        G-->>C: 401
    end
```

### Abertura de Ordem de Serviço

Fluxo de negócio desde a criação da OS até a aprovação do orçamento pelo
cliente, cobrindo a máquina de estados descrita em
[`README.md § Fluxo Principal da Ordem de Servico`](../README.md#fluxo-principal-da-ordem-de-servico-maquina-de-estados)
e as regras de [Reserva de Estoque](../../os-tech-documentation/02%20-%20Regras%20de%20Negocio/Reserva%20de%20Estoque.md)
e [Orçamento](../../os-tech-documentation/02%20-%20Regras%20de%20Negocio/Orcamento.md).

```mermaid
sequenceDiagram
    actor Atendente
    actor Tecnico as Técnico
    actor Cliente
    participant API as Backend (EKS)
    participant Stock as Stock (domínio)
    participant Budget as Budget (domínio)
    participant DB as RDS PostgreSQL
    participant SNS as AWS SNS

    Atendente->>API: POST /order (cliente, veículo, serviços)
    API->>DB: INSERT service_order (status = RECEBIDA)
    API->>SNS: publica notificação (RECEBIDA)
    SNS-->>Cliente: e-mail "OS recebida"
    API-->>Atendente: 201 { serviceOrderId }

    Tecnico->>API: PATCH /order/{id} (EM_DIAGNOSTICO)
    API->>DB: UPDATE service_order SET status = EM_DIAGNOSTICO
    API->>SNS: publica notificação (EM_DIAGNOSTICO)

    Tecnico->>API: POST /api/stocks/reservations (peças/insumos)
    API->>Stock: reserve(quantidade, serviceOrderId) [atômico]
    alt estoque insuficiente
        Stock-->>API: exceção — nenhuma reserva criada
        API-->>Tecnico: 422 estoque insuficiente
    else disponível
        Stock->>DB: INSERT stock_reservations (status = ACTIVE)
        Stock-->>API: reservas confirmadas
        Stock->>Budget: publica ReservationChangedEvent(serviceOrderId)
        Budget->>Stock: findReservations(serviceOrderId, ACTIVE)
        Budget->>DB: UPSERT budgets + budget_items (snapshot de preço)
    end

    Tecnico->>API: PATCH /order/{id} (AGUARDANDO_APROVACAO)
    API->>DB: UPDATE service_order SET status = AGUARDANDO_APROVACAO
    API->>SNS: publica notificação (AGUARDANDO_APROVACAO)
    SNS-->>Cliente: e-mail "orçamento pronto"

    Cliente->>API: GET /api/clients/my-orders/{id} (consulta orçamento)
    API-->>Cliente: 200 { orçamento, itens, total }

    Cliente->>API: POST /api/clients/my-orders/{id}/decision (APPROVED)
    alt aprovado
        API->>DB: UPDATE service_order SET status = APROVADO
        API->>SNS: publica notificação (APROVADO)
    else recusado
        API->>DB: UPDATE service_order SET status = RECUSADA
        API->>Stock: releaseReservation(serviceOrderId)
        Stock->>DB: UPDATE stock_reservations SET status = RELEASED
        API->>SNS: publica notificação (RECUSADA)
        Note over API: OS pode ser reaberta para EM_DIAGNOSTICO
    end
```

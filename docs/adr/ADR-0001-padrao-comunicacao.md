# ADR-0001: Padrão de Comunicação entre Componentes

- **Status:** Aceito
- **Data:** 2026-09-13

## Contexto

O sistema tem dois tipos de comunicação a decidir:

1. **Entre serviços/componentes de infraestrutura** — Cliente → API Gateway →
   Lambda (auth) → Backend (EKS) → RDS.
2. **Dentro do backend, entre módulos de domínio** — por exemplo, quando uma
   reserva de estoque muda, o módulo de Orçamento precisa recalcular o total
   da OS (ver [Reserva de Estoque](../../../os-tech-documentation/02%20-%20Regras%20de%20Negocio/Reserva%20de%20Estoque.md)).

Era preciso decidir se a comunicação seria síncrona (chamada direta/HTTP) ou
assíncrona (fila/broker de mensagens), e se o desacoplamento entre módulos
internos justificava a introdução de um message broker externo (Kafka,
RabbitMQ, SQS).

## Decisão

- **Entre serviços de infraestrutura:** comunicação **síncrona via HTTP/REST**
  (Gateway → Lambda via `AWS_PROXY`; Gateway → Backend via `HTTP_PROXY`). Não
  há fila entre esses saltos — cada requisição do cliente espera a resposta
  de ponta a ponta.
- **Dentro do backend, entre módulos de domínio:** **Spring Application
  Events, síncronos, in-process** (`ApplicationEventPublisher` +
  `@EventListener`), sem broker externo. Exemplo:
  `ReservationChangedEvent` (Stock → Budget), `OrderRejectedEvent`
  (ServiceOrder → liberação de reservas).
- **Notificação ao cliente (mudança de status da OS):** assíncrona via
  **AWS SNS**, mas fora do caminho crítico da requisição — publicada com
  `@Async` e tratada como *best-effort* (falha é logada, não reverte a
  operação de negócio).

## Consequências

- **Simplicidade operacional:** não há broker de mensagens para provisionar,
  monitorar ou versionar contratos de mensagem — reduz a superfície de infra
  a gerenciar em um projeto acadêmico com equipe pequena.
- **Acoplamento temporal aceito nos saltos de infraestrutura:** se a Lambda
  ou o backend estiverem indisponíveis, a requisição falha imediatamente
  (sem retry automático de fila). Aceitável para o volume atual do desafio;
  se o volume de OS crescesse muito, uma fila entre Gateway e Backend seria
  reavaliada.
- **Eventos internos são perdidos se a aplicação cair no meio do processamento**
  (não há persistência de outbox) — aceitável porque o evento
  `ReservationChangedEvent` é reprocessável (o orçamento é sempre recalculado
  do zero a partir das reservas `ACTIVE`, é idempotente).
- **Notificação por e-mail nunca bloqueia o fluxo principal** da OS, graças ao
  uso de `@Async` — uma falha no SNS não impede a transição de status.

## Alternativas Rejeitadas

- **Kafka/RabbitMQ entre módulos internos:** rejeitado por adicionar
  complexidade operacional (cluster, tópicos, consumer groups) sem ganho
  real — os módulos rodam no mesmo processo JVM, e eventos in-process do
  Spring já garantem o desacoplamento de código necessário.
- **SQS entre Gateway e Backend:** rejeitado porque o cliente espera uma
  resposta imediata (aprovação de orçamento, consulta de status) — um
  padrão fire-and-forget não atende ao caso de uso de API síncrona.

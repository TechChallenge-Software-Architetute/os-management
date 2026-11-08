# ADR-0006: Contrato de Mensagens da Saga com o os-orchestrator

- **Status:** Aceito
- **Data:** 2026-11-08
- **Relação com outros ADRs:** detalha o contrato da saga do
  [ADR-0005](ADR-0005-saga-assincrona-via-sqs.md) (filas, nomes e entrega após o
  commit continuam valendo).

## Contexto

O [ADR-0005](ADR-0005-saga-assincrona-via-sqs.md) definiu o transporte (uma fila
de entrada por serviço, SQS com DLQ) e o primeiro passo (`CRIAR_OS`), mas o
contrato completo ficou para quando o `os-orchestrator` tivesse o fluxo. O grupo
fechou esse fluxo na branch `feature/fluxo-com-envio` do `os-orchestrator`:
`StateMachineSteps` define, para cada ação recebida e estado atual, o próximo
estado e a próxima ação a enviar.

Faltava o lado do `os-management`: o que ele envia, o que recebe, o que faz com
cada comando e o que acontece quando um comando não cabe no estado da OS.

## Decisão

- **Formato da mensagem, nos dois sentidos:** corpo = id da OS (texto); atributo
  de mensagem `action` (String) = nome da ação. Os nomes são os do enum
  `StateMachineActions` do orquestrador, espelhado em `SagaAction`.

- **O orquestrador comanda, o serviço executa e responde com a mesma ação.**
  Fluxo de sucesso:

  | # | De → Para | Ação | O que o os-management faz |
  |---|---|---|---|
  | 1 | os-management → orquestrador | `CRIAR_OS` | Envia na abertura da OS (após o commit). |
  | 2 | orquestrador ↔ os-payment | `GERAR_ORCAMENTO` | Nada. O os-management **não** envia mais essa ação. |
  | 3 | orquestrador → os-management | `EXECUTAR_SERVICO` | OS `APROVADO` → `EM_EXECUCAO`; responde `EXECUTAR_SERVICO`. |
  | 4 | orquestrador → os-management | `ATUALIZAR_ESTOQUE` | Confirma (baixa) as reservas da OS; responde `ATUALIZAR_ESTOQUE`. |

- **Compensação:** `NEGAR_SERVICO`, `NEGAR_ESTOQUE` e `ERRO` liberam as reservas
  ativas da OS e levam a OS para o status novo `CANCELADA` (motivo em
  `rejectionReason`). Não há resposta.

- **O `CRIAR_OS` sai na abertura da OS**, como já acontecia. Decisão do grupo.

- **Regras de estado no os-management** (`HandleSagaCommandUseCase`):
  - `EXECUTAR_SERVICO` só é aplicado com a OS `APROVADO`.
  - `ATUALIZAR_ESTOQUE` só é aplicado depois que a execução começou
    (`EM_EXECUCAO`, `FINALIZADA`, `ENTREGUE`).
  - A compensação só cancela antes da execução (`RECEBIDA`, `EM_DIAGNOSTICO`,
    `AGUARDANDO_APROVACAO`, `APROVADO`).

- **Comando repetido não refaz o trabalho.** OS já em execução só tem a
  confirmação reenviada; OS já `CANCELADA` ou `RECUSADA` não é compensada de novo;
  reservas já confirmadas não são baixadas duas vezes.

- **Comando fora do lugar vai para a DLQ.** Ação que não é do os-management
  (`CRIAR_OS`, `GERAR_ORCAMENTO`, `GERAR_PAGAMENTO`), mensagem malformada ou
  comando que não cabe no estado da OS lança exceção: a mensagem não é confirmada,
  volta para a fila e, após o `maxReceiveCount` (5), cai na DLQ.

- **A resposta sai depois do commit**, pelo mesmo `OrderSagaEventListener` do
  `CRIAR_OS`.

- **O cancelamento manual pela API é bloqueado.** `PATCH /order/{id}` com
  `CANCELADA` retorna 400: cancelar fora da compensação deixaria as reservas de
  estoque ativas.

- **O orçamento conta reservas ativas e confirmadas.** Antes só as ativas
  entravam, e a baixa do estoque (`ATUALIZAR_ESTOQUE`) zerava o orçamento aprovado.

## Consequências

- **O os-management só confirma a execução depois do comando.** A oficina ainda
  pode levar a OS para `EM_EXECUCAO` pela API; nesse caso a confirmação
  `EXECUTAR_SERVICO` sai quando o comando do orquestrador chegar.

- **Com o `CRIAR_OS` na abertura, a saga anda na frente da OS.** O orquestrador
  emenda `GERAR_ORCAMENTO`; se o os-payment responder antes de o cliente aprovar
  o orçamento, o `EXECUTAR_SERVICO` chega com a OS em diagnóstico e vai para a
  DLQ. O os-payment precisa esperar a aprovação do cliente. Isso fica fora do
  os-management e precisa ser resolvido com o os-payment.

- **Uma resposta duplicada leva o orquestrador para `ERRO`.** O os-management
  reenvia a confirmação quando recebe um comando repetido (pode ser um
  reprocessamento depois de falha no envio). O orquestrador deveria ignorar a
  confirmação de um passo que já aplicou.

- **A janela entre o commit e o envio continua** (ver ADR-0005): se o SQS falhar
  nesse ponto, a resposta se perde e a saga para. O outbox persistente segue
  como próximo passo.

- **O listener só sobe com `AWS_SQS_ORDER_EVENTS_ENABLED=true`** (ConfigMap do
  deploy AWS). No Kind e nos testes não há SQS.

## Alternativas Rejeitadas

- **Manter o `GERAR_ORCAMENTO` saindo do os-management em `AGUARDANDO_APROVACAO`:**
  rejeitado. No fluxo do orquestrador essa ação é a resposta do os-payment; com
  os dois enviando, o orquestrador recebe a ação duas vezes e vai para `ERRO`.

- **Descartar em silêncio comando fora de ordem:** rejeitado. A saga ficaria
  parada sem sinal nenhum; na DLQ o problema fica visível e a mensagem pode ser
  reprocessada.

- **Compensar depois que a execução começou:** rejeitado. Liberar reservas de
  peças já em uso e cancelar um serviço em andamento exige decisão humana; o
  comando vai para a DLQ.

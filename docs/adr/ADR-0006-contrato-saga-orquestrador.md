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

- **Formato da mensagem, nos dois sentidos:** UUID da OS no corpo da mensagem;
  `action`, `amount` e `correlationId` nos atributos SQS:

  ```json
  corpo: 8f1c...-uuid-da-os
  atributos: action=CRIAR_OS, amount=0.00, correlationId=...
  ```

  - `action`: nome da ação, com os nomes do enum `StateMachineActions` do
    orquestrador (espelhado em `SagaAction`).
  - `amount`: total do orçamento da OS no momento do envio, com 2 casas. O
    `CRIAR_OS` não inicia pagamento; `ORCAMENTO_DISPONIVEL` só é enviado depois
    das reservas e do recálculo de um orçamento positivo.
  - `correlationId`: no `CRIAR_OS`, o `X-Request-Id` da requisição que abriu a OS
    (o mesmo `request_id` dos logs da API); nas respostas, o `correlationId` que
    veio no comando do orquestrador. Cada serviço repassa o que recebe, e a saga
    da OS fica com um único id de ponta a ponta.

- **O orquestrador comanda, o serviço executa e responde com a mesma ação.**
  Fluxo de sucesso:

  | # | De → Para | Ação | O que o os-management faz |
  |---|---|---|---|
  | 1 | os-management → orquestrador | `CRIAR_OS` | Envia na abertura da OS (após o commit); não avança ao pagamento. |
  | 2 | os-management → orquestrador | `ORCAMENTO_DISPONIVEL` | Envia após o commit das reservas e recálculo de valor positivo. |
  | 3 | orquestrador → os-payment | `GERAR_ORCAMENTO` | Cria a cobrança com o valor calculado; a saga aguarda aprovação. |
  | 4 | os-payment → orquestrador | `GERAR_ORCAMENTO` | Confirma a geração da cobrança, sem iniciar execução. |
  | 5 | os-management → orquestrador | `ORCAMENTO_APROVADO` | Envia quando o cliente aprova a OS. |
  | 6 | os-payment → orquestrador | `PAGAMENTO_APROVADO` | Sinaliza aprovação do pagamento. A execução só segue após as duas aprovações. |
  | 7 | orquestrador → os-management | `EXECUTAR_SERVICO` | OS `APROVADO` → `EM_EXECUCAO`; responde `EXECUTAR_SERVICO`. |
  | 8 | orquestrador → os-management | `ATUALIZAR_ESTOQUE` | Confirma (baixa) as reservas da OS; responde `ATUALIZAR_ESTOQUE`. |

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
  (`CRIAR_OS`, `ORCAMENTO_DISPONIVEL`, `ORCAMENTO_APROVADO`, `PAGAMENTO_APROVADO`,
  `GERAR_ORCAMENTO`, `GERAR_PAGAMENTO`), mensagem malformada ou
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

- **O pagamento começa após o orçamento positivo.** `CRIAR_OS` apenas registra
  a OS no orquestrador. Reservas confirmadas e orçamento calculado liberam
  `ORCAMENTO_DISPONIVEL`; mensagens duplicadas dessa ação são ignoradas nos
  estados de geração e orçamento gerado.

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

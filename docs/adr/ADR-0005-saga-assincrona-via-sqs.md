# ADR-0005: Saga Assíncrona via SQS entre Serviços

- **Status:** Aceito
- **Data:** 2026-11-01
- **Relação com outros ADRs:** complementa o
  [ADR-0001](ADR-0001-padrao-comunicacao.md) (não o substitui — ver *Contexto*)
  e aplica o contrato de deploy independente do
  [ADR-0003](ADR-0003-split-multiplos-repositorios.md).

## Contexto

O sistema ganhou dois serviços novos: o `os-orchestrator`, que mantém a máquina
de estados da ordem de serviço, e o `os-payment`, que trata pagamentos. O fluxo
de uma OS deixou de viver inteiro dentro do `os-management`.

O [ADR-0001](ADR-0001-padrao-comunicacao.md) rejeitou explicitamente "SQS entre
Gateway e Backend", com o argumento de que o cliente espera resposta imediata
(aprovação de orçamento, consulta de status) e que fire-and-forget não atende a
uma API síncrona.

**Esse argumento continua válido e essa decisão não está sendo revertida.** O
caminho do cliente segue síncrono: Cliente → API Gateway → `os-management`, por
`HTTP_PROXY`, com resposta de ponta a ponta. O que o ADR-0001 não previa é um
fluxo de negócio *atrás* do roteador, entre serviços, onde nenhum cliente está
esperando: o avanço da saga entre criação da OS, orçamento, execução e
pagamento.

Era preciso decidir como esses serviços conversam, e como garantir que o deploy
de um não passe a depender do deploy do outro.

## Decisão

- **O `os-management` continua sendo o roteador HTTP do sistema.** Nenhuma rota
  nova foi adicionada ao API Gateway e o `ANY /{proxy+}` continua apontando para
  ele. O `os-orchestrator` **não** recebe rota, controller REST nem exposição
  HTTP: é acionado exclusivamente por mensagens.

- **Comunicação entre serviços da saga: assíncrona via AWS SQS.** Cada serviço
  possui **apenas a sua fila de entrada**, com DLQ e redrive próprios:

  | Fila | Repositório dono | Produtor |
  |---|---|---|
  | `os-orchestrator-<env>-order-processing` | `os-orchestrator` | `os-management` (início da saga) |
  | `os-payment-<env>-payment-requests` | `os-payment` | `os-orchestrator` |
  | `os-management-<env>-order-events` | `os-management` | `os-orchestrator` (resultado) |

- **Nomes determinísticos, nunca `terraform_remote_state`.** O padrão é
  `os-<serviço>-<env>-<propósito>`, com `<env>` igual ao nome da branch. Uma fila
  de outro serviço é endereçada montando o nome (e, quando necessário, o ARN a
  partir de `aws_caller_identity` e da região). Nenhuma stack lê o state de outra.

- **Nenhuma inscrição em fila de terceiro no momento do deploy.** Não existe
  `aws_lambda_event_source_mapping` nem `aws_sqs_queue_policy` sobre fila de
  outro repositório, porque isso exigiria que o recurso do outro já existisse. O
  consumidor faz polling em runtime, que só precisa da URL quando roda.

- **Autorização pela policy IAM do produtor, não por resource policy.** Produtor
  na mesma conta recebe `sqs:SendMessage` pela própria policy. A variável
  `producer_role_arns` existe em cada stack para o caso cross-account e vem
  **vazia** por padrão, sem nenhuma resource policy criada. Wildcard é barrado
  por `validation` no Terraform.

- **O `os-management` continua no EKS, sem Lambda nova.** A conversão para
  Lambda foi descartada: é um monólito JPA/Hibernate sobre RDS, e o `os-payment`
  já cobre a exigência de função serverless no fluxo novo — além do issuer e do
  authorizer que já existem no `os-management-lambda`.

- **O comando da saga sai depois do commit.** Os use cases publicam um evento
  interno (`OrderSagaEvent`) e um `@TransactionalEventListener(AFTER_COMMIT)`
  faz o envio ao SQS. Os eventos in-process do ADR-0001 continuam sendo o
  mecanismo entre módulos do mesmo processo; aqui eles ganham o papel adicional
  de ponte até a fila.

## Consequências

- **Os deploys ficaram de fato independentes.** Qualquer repositório aplica em
  qualquer ordem, inclusive primeiro e em conta vazia. A única dependência real
  que existia — o `os-management-gateway` lendo o state do `os-management-lambda`,
  com um passo de CI que falhava sem ele — foi removida no mesmo trabalho.

- **Consistência eventual no fluxo da saga.** O estado da OS no `os-management`
  e o estado na máquina do `os-orchestrator` convergem, mas não
  instantaneamente. O caminho de leitura do cliente não é afetado, porque
  continua síncrono.

- **Mensagem perdida vira mensagem na DLQ, não trabalho perdido.** Cada fila tem
  DLQ com `maxReceiveCount` 5 e retenção de 14 dias, então uma falha de
  processamento é inspecionável em vez de silenciosa — ganho real sobre os
  eventos in-process, que se perdem se o processo cair.

- **Uma mudança de estado commitada não é desfeita por falha de envio.** O envio
  acontece após o commit; se o SQS falhar, o erro é logado e medido
  (`integrationFailed("aws_sqs")`) sem reverter a operação de negócio — mesmo
  tratamento já usado para o SNS. Em troca, existe a janela em que o estado
  mudou e o comando não saiu. Para o volume do desafio isso é aceitável; um
  outbox persistente é o próximo passo se deixar de ser.

- **A criação da OS é atômica.** O `CreateOrderUseCase` é `@Transactional`: a
  OS e os serviços dela entram juntos ou não entram, e o `CRIAR_OS` só sai
  depois do commit. O `fallbackExecution = true` do listener continua como rede
  de segurança para qualquer caminho que publique o evento fora de transação.

- **Contrato de mensagem é acoplamento, e ele é mínimo por escolha.** Corpo =
  id da ordem; atributo `action` = nome da ação. Os nomes das ações precisam
  casar entre o enum `SagaAction` (`os-management`) e `StateMachineActions`
  (`os-orchestrator`) — é o ponto que quebra se um lado renomear sozinho.

- **A tradução estado → ação fica em um único mapa.** O `os-management` tem oito
  estados e o orquestrador três ações, então a maioria das transições não gera
  comando. Acrescentar uma ação futura (`FINALIZADA`, `ENTREGUE`, `RECUSADA`) é
  uma linha no enum e uma no `SagaActionResolver`: os três pontos de publicação
  já estão ligados.

- **Sem IRSA, os pods usam chaves estáticas em Secret.** O
  `os-management-k8s-terraform` não habilita OIDC, então a permissão de SQS chega
  pela role dos nodes (ou por chaves no Secret), o que a concede a todos os pods
  do cluster. Habilitar `enable_irsa` e usar service-account role é a correção
  registrada como pendência.

## Alternativas Rejeitadas

- **Rotear todas as rotas pelo `os-orchestrator`:** rejeitado. A máquina de
  estados tem três passos e não conhece veículos, estoque nem usuários;
  colocá-la na frente de ~50 endpoints a transformaria em fachada de monólito
  distribuído. Leituras (`GET /my-orders`, Swagger, `/actuator/health`) não têm
  saga, e fazer todo o tráfego transitar por ela tornaria o deploy dela
  dependência de runtime de tudo — o oposto do objetivo.

- **Rotas novas no API Gateway apontando para o `os-orchestrator`:** rejeitado
  junto com a decisão de manter o `os-management` como roteador. Evita um
  segundo caminho de entrada para autenticar, limitar e observar.

- **API Gateway → SQS direto (integração `AWS` service proxy):** rejeitado por
  deixar o cliente sem resposta de negócio e por mover a tradução
  requisição → comando para um template de mapeamento, fora de teste unitário.

- **Lambda de ponte entre o Gateway e a fila:** rejeitado por adicionar um salto
  e um artefato de deploy sem nada que o `os-management` já não faça.

- **`terraform_remote_state` entre as stacks novas:** rejeitado — é exatamente o
  acoplamento que o ADR-0003 quer evitar, e foi o que travava o deploy do
  gateway.

- **Converter o `os-management` para SQS + Lambda:** rejeitado. Significaria cold
  start de JVM + Hibernate, RDS Proxy para pooling de conexões, e perda do HPA
  e da injeção do tracer pelo admission controller do Datadog. As leituras
  continuam precisando ser síncronas, então seria reescrita, não migração.

- **Kafka/RabbitMQ para a saga:** rejeitado pelo mesmo motivo do ADR-0001 —
  cluster, tópicos e consumer groups para três filas de baixo volume. O SQS é
  gerenciado, tem DLQ nativa e já está no ferramental da conta.

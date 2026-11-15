# Datadog

Esta pasta versiona o dashboard e os monitores do `os-management`. A instalação do Agent e a configuração do pod ficam em `k8s/`, e a pipeline aplica tudo.

## Como a integração funciona

| Sinal | Caminho | Onde está configurado |
|---|---|---|
| Traces (APM) | O admission controller do Cluster Agent injeta o `dd-java-agent` no pod; o tracer envia os spans para o Node Agent do mesmo nó (`DD_AGENT_HOST` = IP do nó, porta 8126) | `k8s/datadog/values.yaml`; label e annotation em `k8s/app.yaml` |
| Logs | JSON no stdout, coletado pelo Node Agent pela annotation `ad.datadoghq.com/os-management.logs`; `dd.trace_id`/`dd.span_id` entram no MDC pelo `DD_LOGS_INJECTION` | `logback-spring.xml`, `k8s/app.yaml` |
| Métricas de infraestrutura | O Node Agent lê o kubelet (`kubernetes.cpu.usage.total`, `kubernetes.memory.usage`) e roda o `http_check` da annotation (`http.can_connect`) | `k8s/app.yaml` |
| Métricas de negócio | O Micrometer (`micrometer-registry-datadog`) envia direto para `https://api.datadoghq.eu` a cada minuto, sem passar pelo Agent | `application.yml`, `ServiceOrderMetrics`, ConfigMap |
| Dashboard e monitores | O job `datadog-config` roda `.github/scripts/apply-datadog.sh` (upsert por título/nome) | esta pasta |

## Pré-requisitos

- A conta Datadog é do site **EU** (`datadoghq.eu`). Agent, Micrometer e script de importação usam esse site por padrão; a variável de repositório `DD_SITE` só é necessária se a conta mudar de site.
- Secrets do repositório: `DD_API_KEY` (Agent e Micrometer) e `DD_APP_KEY` (só para importar dashboard e monitores; precisa de escrita nos dois). Sem `DD_API_KEY`, a pipeline sobe a app sem Agent; sem `DD_APP_KEY`, o job `datadog-config` é ignorado.
- Troque `@team-oncall` nos monitores pelo destino de alerta da equipe.

## Injeção do tracer

O job `deploy-aws` instala o Agent pelo Helm (chart `datadog/datadog`, valores em `k8s/datadog/values.yaml`) antes da aplicação. O admission controller roda com `mutateUnlabelled: false`, então só altera pods que tenham **as duas** marcações:

```yaml
labels:
  admission.datadoghq.com/enabled: "true"            # libera o webhook para este pod
annotations:
  admission.datadoghq.com/java-lib.version: "latest" # biblioteca que será injetada
```

Sem a label, o pod sobe normalmente, mas sem tracer: o APM fica vazio, o painel de latência e o monitor de p95 ficam sem dados e os logs chegam sem `dd.trace_id`.

A injeção só acontece em pods criados depois que o Cluster Agent está pronto. Por isso a pipeline espera o `datadog-cluster-agent` e o DaemonSet `datadog` antes de aplicar a app e, no fim do deploy, confere se o pod tem o init container `datadog-lib-java-init`. Se algum desses passos falhar, o deploy segue e o job mostra um aviso.

## Ambientes

O `env` vem da branch, mapeado no job `deploy-aws`:

| Branch | `env` |
|---|---|
| `main` | `production` |
| `develop` | `homolog` |
| outras | `developer` |

A pipeline grava esse valor no `DD_ENV` do ConfigMap (traces, logs e Micrometer) e na label `tags.datadoghq.com/env` do pod (métricas `kubernetes.*` do Agent). No dashboard, escolha o ambiente na variável `env`; o padrão é `production`. Os monitores filtram `env:production`, então em `homolog` eles ficam sem dados e a validação é feita pelo dashboard e pelo APM.

O deploy local em Kind (`deploy-local`) não serve para validar o Datadog: o Secret usa `DD_API_KEY: "REPLACE_ME"` (o Micrometer recebe 403), o `env` é `local` e o cluster é apagado no fim do job.

## Importação manual

O job `datadog-config` já faz o upsert em `develop` e `main`. Para aplicar à mão (requer `jq` e `curl`):

```sh
export DD_API_KEY=... DD_APP_KEY=... DD_SITE=datadoghq.eu
bash .github/scripts/apply-datadog.sh
```

O monitor `uptime.json` usa o check HTTP interno do Agent. Para disponibilidade externa, crie também um teste HTTP no Datadog Synthetics apontando para uma URL pública.

## Teste local (Kind, sem AWS)

O script `k8s/datadog/test-local.sh` sobe um cluster Kind com o Datadog Agent, Postgres e a app construída a partir do código local, no mesmo modelo do EKS (admission controller + label do pod). Valida a injeção do tracer, traces, logs, métricas do kubelet e do Micrometer e o dashboard. Não cobre a fila SQS nem os passos do job `deploy-aws`.

Requer Docker rodando, `kind`, `kubectl` e `helm` (`winget install Kubernetes.kind Kubernetes.kubectl Helm.Helm`). No Git Bash, na raiz do repositório:

```sh
export DD_API_KEY=...              # obrigatória
export DD_APP_KEY=...              # opcional: importa dashboard e monitores (requer jq)
export DD_SITE=datadoghq.eu        # padrão; troque se a conta for de outro site
bash k8s/datadog/test-local.sh up        # cluster, Agent, imagem local, app e verificações
bash k8s/datadog/test-local.sh traffic   # port-forward + validation.sh + links do Datadog
bash k8s/datadog/test-local.sh down      # apaga o cluster
```

Os dados chegam com `env:developer` (um dos valores do filtro do dashboard). O `validation.sh` cadastra o cliente João; numa segunda execução contra o mesmo banco ele para nesse passo, mas as chamadas anteriores já geram dados.

## Como validar (EKS da `develop`, `env:homolog`)

1. **Credenciais**

   ```sh
   curl -s "https://api.datadoghq.eu/api/v1/validate" -H "DD-API-KEY: $DD_API_KEY"   # {"valid":true}
   ```

2. **Agent**

   ```sh
   aws eks update-kubeconfig --name os-management-develop --region us-east-1
   kubectl get pods -n datadog -o wide
   kubectl exec -n datadog ds/datadog -c agent -- agent status
   ```

   No `agent status`, a API key tem que aparecer como válida e as seções "APM Agent" e "Logs Agent" têm que estar ativas.

3. **Tracer injetado**

   ```sh
   kubectl get pods -n os-management -l app=os-management \
     -o jsonpath='{.items[*].spec.initContainers[*].name}'          # datadog-lib-java-init
   kubectl logs -n os-management -l app=os-management --tail=-1 | grep -m1 "DATADOG TRACER CONFIGURATION"
   ```

4. **Tráfego**: rode algumas requisições da coleção do Bruno (`bruno/`), incluindo a criação de uma OS.

5. **No Datadog**, filtrando `service:os-management env:homolog`:
   - APM → Services: o serviço aparece com os endpoints chamados.
   - Logs: as linhas trazem `dd.trace_id`; a partir de um trace, a aba Logs mostra os logs da mesma requisição.
   - Metrics Explorer: `workshop.service_orders.created` (pode levar até 1 minuto, o step do Micrometer) e `kubernetes.cpu.usage.total`.
   - Dashboard "OS Management | Operação" com `env = homolog`.

6. **Saga**: ao criar uma OS, o tracer propaga o contexto no atributo `_datadog` da mensagem SQS. O trace só continua no os-orchestrator se o pod dele também tiver a label `admission.datadoghq.com/enabled`, e para na Lambda do os-payment, que não tem Datadog.

## Cobertura

- APM fornece latência HTTP e traces; health endpoints alimentam readiness/liveness e uptime.
- O Agent coleta `kubernetes.cpu.usage.total` e `kubernetes.memory.usage`.
- Métricas de negócio: `workshop.service_orders.created`, `workshop.service_orders.status.duration`, `workshop.service_orders.processing.failed` e `workshop.integrations.failed`.

## Logs estruturados e correlação

A aplicação emite logs em JSON (logback + `logstash-logback-encoder`) e o Agent
os coleta com `source:java` (annotation `ad.datadoghq.com/os-management.logs`),
que ativa o parsing automático do pipeline Java do Datadog. Campos padronizados
em cada linha:

| Campo | Origem | Uso |
|---|---|---|
| `@timestamp`, `level`, `logger_name`, `thread_name`, `message` | providers do logback | atributos reservados do Datadog |
| `request_id` | `RequestCorrelationFilter` (MDC) | correlação por requisição; também devolvido no header `X-Request-Id` |
| `user` | `JwtFilter` (MDC, após autenticação) | principal autenticado |
| `dd.trace_id`, `dd.span_id` | `dd-java-agent` via `DD_LOGS_INJECTION=true` | pivô Logs ↔ APM |

O `request_id` é propagado para threads de `@Async` pelo `MdcTaskDecorator`, de
modo que logs em background mantêm a mesma correlação da requisição de origem.

### Facet do `request_id`

`dd.trace_id`/`dd.span_id` já correlacionam logs e traces automaticamente. Para
buscar/agrupar por `request_id` (e por `user`), promova-os a facets uma vez por
org — é uma ação de UI, não versionável via API:

1. Logs → abra uma linha que contenha `request_id`.
2. No painel do atributo, clique em `request_id` → **Create facet** (tipo String).
3. Repita para `user`.

Depois disso, filtre ponta a ponta com `service:os-management @request_id:<id>`
e pivote de um trace do APM para os logs da mesma requisição.

# Datadog

Esta pasta versiona os artefatos de observabilidade do `os-management`.

## Pré-requisitos no cluster

Instale o Datadog Agent com a integração Kubernetes, APM, logs e métricas de kube-state-metrics habilitados. O Agent deve aceitar tráfego APM na porta 8126 em cada nó. Crie o segredo da aplicação com `DD_API_KEY` (não versione uma chave real) e substitua `@team-oncall` nos monitores pelo destino de alerta da equipe. O manifesto kustomize habilita a exportação direta de métricas; portanto, não aplique `k8s/secret.yaml` com o valor de exemplo em ambientes reais.

O `initContainer` em `k8s/base/app.yaml` fornece o agente Java; a aplicação recebe `DD_SERVICE`, `DD_ENV` e `DD_VERSION`. A correlação APM/log ocorre por `dd.trace_id` e `dd.span_id` injetados pelo agente, e cada resposta também devolve `X-Request-Id` para correlação de ponta a ponta.

## Ambientes

O dashboard possui o seletor `env` com os ambientes `developer`, `homolog` e `production`. A mesma tag `env` é enviada para traces, logs e métricas pelo `DD_ENV` e pela label `tags.datadoghq.com/env`; portanto, em APM e Logs filtre por `service:os-management env:<ambiente>`.

Para aplicar o ambiente em um cluster, use somente um overlay por vez:

```sh
kubectl apply -k k8s/overlays/developer
# ou
kubectl apply -k k8s/overlays/homolog
# ou
kubectl apply -k k8s/overlays/production
```

Cada ambiente deve usar seu próprio cluster ou namespace, para que os recursos com o mesmo nome não colidam. Depois do rollout, selecione o valor correspondente na variável `env` do dashboard.

No fluxo Terraform local, defina `datadog_environment = "developer"`, `"homolog"` ou `"production"` no `terraform.tfvars`. O valor padrão é `production` para preservar instalações existentes.

## Importação

Com `DD_API_KEY`, `DD_APP_KEY` e `DD_SITE` definidos, importe o dashboard e cada monitor pela API do Datadog. A Application Key precisa ter permissão de escrita para dashboards e monitores:

```sh
curl -X POST "https://api.${DD_SITE}/api/v1/dashboard" -H "DD-API-KEY: ${DD_API_KEY}" -H "DD-APPLICATION-KEY: ${DD_APP_KEY}" -H "Content-Type: application/json" -d @datadog/dashboards/os-management.json
curl -X POST "https://api.${DD_SITE}/api/v1/monitor" -H "DD-API-KEY: ${DD_API_KEY}" -H "DD-APPLICATION-KEY: ${DD_APP_KEY}" -H "Content-Type: application/json" -d @datadog/monitors/os-processing-failures.json
```

Repita o segundo comando para os demais arquivos em `monitors/`. O monitor `uptime.json` usa o check HTTP interno do Agent. Para disponibilidade externa, crie também um teste HTTP no Datadog Synthetics apontando para uma URL pública; `localhost` e o IP privado do Kind exigem uma Private Location.

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

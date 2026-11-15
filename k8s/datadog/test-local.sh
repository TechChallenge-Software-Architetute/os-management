#!/usr/bin/env bash
# Sobe o os-management num cluster Kind local com o Datadog Agent, para validar
# a integração sem precisar da AWS:
#   - injeção do dd-java-agent pelo admission controller (label em k8s/app.yaml);
#   - traces, logs com dd.trace_id, métricas do kubelet e do Micrometer;
#   - dashboard (se DD_APP_KEY estiver definida).
# O que NÃO dá para ver aqui: a fila SQS (o envio para o orquestrador fica
# desligado sem AWS_SQS_ORCHESTRATOR_QUEUE_URL) e os passos do job deploy-aws.
#
# Uso (Git Bash, na raiz do repositório):
#   export DD_API_KEY=<api key>          # obrigatória para "up"
#   export DD_APP_KEY=<app key>          # opcional: importa o dashboard e os monitores
#   export DD_SITE=datadoghq.com         # opcional: padrão datadoghq.eu (conta do time)
#   bash k8s/datadog/test-local.sh up        # cluster, Agent, imagem local e app
#   bash k8s/datadog/test-local.sh traffic   # port-forward + validation.sh + links
#   bash k8s/datadog/test-local.sh check     # repete as verificações
#   bash k8s/datadog/test-local.sh diag      # diagnóstico do Agent do nó
#   bash k8s/datadog/test-local.sh down      # apaga o cluster
#
# Requer Docker rodando, kind, kubectl e helm
# (winget install Kubernetes.kind Kubernetes.kubectl Helm.Helm).
set -euo pipefail

CLUSTER="${CLUSTER:-os-management}"
DD_SITE="${DD_SITE:-datadoghq.eu}"
DD_ENV="${DD_ENV:-developer}"   # um dos valores do filtro env do dashboard
IMAGE="${IMAGE:-os-management:dev}"
NS=os-management
PF_PID=""

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

log() { printf '\n==> %s\n' "$*"; }

require() {
  local c
  for c in "$@"; do
    command -v "$c" >/dev/null 2>&1 || { echo "Comando '$c' não encontrado no PATH." >&2; exit 1; }
  done
}

# URL da interface web: app.<site> para US1/EU/gov, <site> para us3/us5/ap1/ap2.
app_url() {
  case "$DD_SITE" in
    datadoghq.com|datadoghq.eu|ddog-gov.com) echo "https://app.$DD_SITE" ;;
    *) echo "https://$DD_SITE" ;;
  esac
}

up() {
  : "${DD_API_KEY:?Defina DD_API_KEY antes de rodar (export DD_API_KEY=...)}"
  require docker kind kubectl helm

  # O kind usa Kubernetes 1.35+, cujo kubelet não sobe em cgroup v1 (failCgroupV1).
  local cg
  cg="$(docker info --format '{{.CgroupVersion}}' 2>/dev/null || true)"
  if [ "$cg" = "1" ]; then
    echo "O Docker está usando cgroup v1, e o Kubernetes do kind (1.35+) não sobe nele." >&2
    echo "No Windows, adicione ao arquivo %UserProfile%\\.wslconfig:" >&2
    echo "  [wsl2]" >&2
    echo "  kernelCommandLine = cgroup_no_v1=all" >&2
    echo "Depois feche o Docker Desktop, rode 'wsl --shutdown' e abra o Docker Desktop de novo." >&2
    echo "Confira com: docker info --format '{{.CgroupVersion}}'   (precisa mostrar 2)" >&2
    exit 1
  fi

  log "Cluster Kind '$CLUSTER'"
  local clusters
  clusters="$(kind get clusters 2>/dev/null || true)"
  if grep -qx "$CLUSTER" <<<"$clusters"; then
    echo "Cluster já existe, reutilizando."
  else
    kind create cluster --name "$CLUSTER"
  fi
  kubectl config use-context "kind-$CLUSTER" >/dev/null

  log "Datadog Agent e Cluster Agent (site $DD_SITE)"
  kubectl create namespace datadog --dry-run=client -o yaml | kubectl apply -f -
  kubectl create secret generic datadog-secret -n datadog \
    --from-literal api-key="$DD_API_KEY" --dry-run=client -o yaml | kubectl apply -f -
  helm repo add datadog https://helm.datadoghq.com >/dev/null 2>&1 || true
  helm repo update datadog >/dev/null
  # tlsVerify=false só aqui: o kubelet do Kind usa certificado autoassinado e,
  # sem isso, as métricas kubernetes.* (CPU/memória) não chegam.
  helm upgrade --install datadog datadog/datadog -n datadog -f k8s/datadog/values.yaml \
    --set datadog.site="$DD_SITE" \
    --set datadog.clusterName="${CLUSTER}-local" \
    --set datadog.kubelet.tlsVerify=false
  # A injeção só acontece em pods criados depois que o Cluster Agent está pronto.
  kubectl rollout status deployment/datadog-cluster-agent -n datadog --timeout=300s
  # O Agent do nó não bloqueia o teste: a injeção depende só do Cluster Agent.
  # Se ele não ficar pronto, mostra o diagnóstico e segue com o deploy da app.
  if ! kubectl rollout status daemonset/datadog -n datadog --timeout=180s; then
    echo "AVISO: o Agent do nó não ficou pronto; seguindo com a app. Diagnóstico:" >&2
    agent_diag
  fi

  log "Imagem $IMAGE a partir do código desta branch"
  docker build -t "$IMAGE" .
  kind load docker-image "$IMAGE" --name "$CLUSTER"

  log "Postgres e app (kustomize de k8s/)"
  kubectl apply -k k8s/
  # O secret.yaml traz DD_API_KEY=REPLACE_ME; sem a chave real o Micrometer recebe 403.
  kubectl patch secret os-management-secret -n "$NS" --type merge \
    -p "{\"stringData\":{\"DD_API_KEY\":\"$DD_API_KEY\"}}"
  # 'local' não está no filtro env do dashboard; o site precisa bater com a conta.
  kubectl patch configmap os-management-config -n "$NS" --type merge \
    -p "{\"data\":{\"DD_ENV\":\"$DD_ENV\",\"DD_METRICS_URI\":\"https://api.$DD_SITE\"}}"
  # Imagem local (IfNotPresent: com Always o Kind tentaria baixar do Docker Hub)
  # e label de env, que o Agent usa para as métricas kubernetes.*.
  kubectl patch deployment os-management -n "$NS" -p "{\"spec\":{\"template\":{
    \"metadata\":{\"labels\":{\"tags.datadoghq.com/env\":\"$DD_ENV\"}},
    \"spec\":{\"containers\":[{\"name\":\"os-management\",\"image\":\"$IMAGE\",\"imagePullPolicy\":\"IfNotPresent\"}]}}}}"
  # Pod novo mesmo quando o patch não muda nada (reexecução com a mesma tag),
  # para pegar a imagem recém-construída, o secret e o configmap.
  kubectl rollout restart deployment/os-management -n "$NS"
  # A readinessProbe começa em 150 s.
  kubectl rollout status deployment/os-management -n "$NS" --timeout=480s

  dashboard
  check
}

dashboard() {
  if [ -z "${DD_APP_KEY:-}" ]; then
    log "Dashboard e monitores: pulado (defina DD_APP_KEY para importar)"
    return 0
  fi
  if ! command -v jq >/dev/null 2>&1; then
    log "Dashboard e monitores: pulado (instale o jq: winget install jqlang.jq)"
    return 0
  fi
  log "Dashboard e monitores (apply-datadog.sh)"
  DD_SITE="$DD_SITE" bash .github/scripts/apply-datadog.sh \
    || echo "AVISO: falha ao importar dashboard/monitores; veja a mensagem acima." >&2
}

check() {
  require kubectl
  log "Verificações"
  local init logs
  init="$(kubectl get pods -n "$NS" -l app=os-management \
    -o jsonpath='{.items[*].spec.initContainers[*].name}' 2>/dev/null || true)"
  if grep -q "datadog-lib-java" <<<"$init"; then
    echo "OK     dd-java-agent injetado (init containers: $init)"
  else
    echo "ERRO   pod sem o init container datadog-lib-java-init: o tracer não foi injetado." >&2
    echo "       Confira a label admission.datadoghq.com/enabled em k8s/app.yaml e os pods abaixo." >&2
  fi

  logs="$(kubectl logs -n "$NS" deploy/os-management --tail=-1 2>/dev/null || true)"
  if grep -q "DATADOG TRACER CONFIGURATION" <<<"$logs"; then
    echo "OK     tracer iniciou junto com a aplicação"
  else
    echo "AVISO  a linha 'DATADOG TRACER CONFIGURATION' não apareceu no log da app"
  fi
  if grep -qi "failed to send metrics to datadog" <<<"$logs"; then
    echo "AVISO  o Micrometer não conseguiu enviar métricas: confira DD_API_KEY e DD_SITE"
  fi

  echo
  kubectl get pods -n datadog -o wide
  agent_diag
  echo
  echo "Próximo passo: bash k8s/datadog/test-local.sh traffic"
}

# Mostra por que o Agent do nó não está pronto: containers, 'agent health' e a
# validação da API key (chave inválida ou de outro site é a causa mais comum).
agent_diag() {
  local pod
  pod="$(kubectl get pods -n datadog --no-headers -o custom-columns=:metadata.name 2>/dev/null \
    | grep -v -e cluster-agent -e operator | head -1 || true)"
  if [ -z "$pod" ]; then
    echo "Pod do Agent não encontrado no namespace datadog." >&2
    return 0
  fi
  echo
  echo "Agent $pod, containers prontos:"
  kubectl get pod -n datadog "$pod" \
    -o jsonpath='{range .status.containerStatuses[*]}  {.name}: {.ready}{"\n"}{end}' || true
  echo "agent health:"
  { kubectl exec -n datadog "$pod" -c agent -- agent health 2>&1 || true; } | sed 's/^/  /'
  echo "API key (agent status):"
  { kubectl exec -n datadog "$pod" -c agent -- agent status 2>/dev/null || true; } \
    | grep -i -A1 "api key" | sed 's/^/  /' || true
}

stop_port_forward() {
  [ -n "$PF_PID" ] && kill "$PF_PID" 2>/dev/null || true
}

traffic() {
  require kubectl curl
  log "Port-forward localhost:8080 -> svc/os-management"
  kubectl port-forward -n "$NS" svc/os-management 8080:8080 >/dev/null 2>&1 &
  PF_PID=$!
  trap stop_port_forward EXIT
  local i
  for i in $(seq 1 30); do
    curl -s -o /dev/null http://localhost:8080 && break
    sleep 1
  done

  log "validation.sh (fluxo completo da OS)"
  # O validation.sh cadastra o cliente João; numa segunda execução contra o
  # mesmo banco ele para nesse passo, mas as chamadas anteriores já geram dados.
  APP_URL=http://localhost:8080 sh validation.sh \
    || echo "AVISO: o validation.sh parou antes do fim (veja a etapa acima)." >&2

  log "Onde ver no Datadog (os dados levam de 1 a 2 minutos para aparecer)"
  links
}

links() {
  local app
  app="$(app_url)"
  echo "APM:        $app/apm/services?env=$DD_ENV"
  echo "Logs:       $app/logs?query=service%3Aos-management%20env%3A$DD_ENV"
  echo "Métricas:   $app/metric/explorer   (workshop.service_orders.created, filtro env:$DD_ENV)"
  echo "Dashboard:  $app/dashboard/lists   (\"OS Management | Operação\", env = $DD_ENV)"
  echo "Containers: $app/containers"
}

down() {
  require kind
  kind delete cluster --name "$CLUSTER"
}

case "${1:-up}" in
  up) up ;;
  check) check ;;
  traffic) traffic ;;
  dashboard) dashboard ;;
  diag) agent_diag ;;
  links) links ;;
  down) down ;;
  *) echo "Uso: $0 [up|traffic|check|diag|dashboard|links|down]" >&2; exit 2 ;;
esac

# ADR-0002: Uso de HPA para Escalabilidade Horizontal

- **Status:** Aceito
- **Data:** 2026-09-13

## Contexto

Com a expansão da oficina para múltiplas unidades, o volume de chamadas à API
(consulta de status, abertura de OS, aprovação de orçamento) varia ao longo
do dia e entre unidades. O requisito obrigatório da Fase 3 pede um "Cluster
Kubernetes com escalabilidade" e o monitoramento de "consumo de recursos do
Kubernetes (CPU, memória)". Era preciso decidir **como** escalar os pods da
aplicação diante de picos de carga.

## Decisão

Usar o **HorizontalPodAutoscaler (HPA)** nativo do Kubernetes, configurado em
`k8s/hpa.yaml` no repositório `os-management`:

- Métricas: **CPU 70%** e **memória 75%** de utilização média dos pods.
- Réplicas: mínimo **1**, máximo **6** (ambiente local usa mínimo 2 — ver
  `README.md § Arquitetura da Infraestrutura`).
- Comportamento assimétrico: *scale-up* agressivo (dobra os pods em ~60s),
  *scale-down* conservador (aguarda ~5 min de estabilização antes de reduzir),
  para evitar oscilação (*flapping*) sob carga intermitente.
- Pré-requisito de cluster: o **metrics-server** (via Helm) é provisionado
  pelo repositório `os-management-k8s-terraform`, pois o HPA depende dele
  para ler métricas de CPU/memória dos pods.

## Consequências

- Escalabilidade automática e **sem intervenção manual** — o time não precisa
  prever o pico de carga com antecedência nem redimensionar o `Deployment`
  manualmente.
- A métrica de decisão é **infraestrutura (CPU/memória)**, não uma métrica de
  negócio (ex.: OS/minuto) — mais simples de operar, mas menos precisa para
  prever gargalos específicos do domínio. Métricas de negócio (
  `workshop.service_orders.created`, `.status.duration`) são monitoradas
  separadamente via Datadog para complementar essa visão (ver
  `os-management/datadog/README.md`).
- **Dependência explícita do metrics-server** — se o cluster não o tiver
  instalado, o HPA fica com métricas `<unknown>` e não escala; por isso a
  responsabilidade de instalá-lo foi fixada no repositório de infraestrutura
  do cluster (`os-management-k8s-terraform`), não no repositório da aplicação.
- Válido tanto no modo local (Kind, `postgres` in-cluster) quanto no modo AWS
  (EKS + RDS externo) — o manifesto `hpa.yaml` é o mesmo nos dois ambientes.

## Alternativas Rejeitadas

- **Vertical Pod Autoscaler (VPA):** rejeitado porque redimensionar CPU/memória
  por pod não resolve picos de requisições concorrentes tão bem quanto
  aumentar o número de réplicas atrás do `Service`/`Ingress`.
- **Escalonamento manual (`kubectl scale`) via pipeline agendado:** rejeitado
  por exigir prever a janela de pico com antecedência — não atende à demanda
  de múltiplas unidades com padrões de tráfego distintos.
- **KEDA (autoscaling orientado a eventos):** avaliado, mas descartado por
  adicionar um operador extra ao cluster sem necessidade — não há filas ou
  métricas externas (ex.: profundidade de fila SQS) que justifiquem escalar
  por evento em vez de CPU/memória neste projeto.

apiVersion: v1
kind: ConfigMap
metadata:
  name: os-management-config
  namespace: os-management
data:
  SERVER_PORT: "8080"
  SPRING_APPLICATION_NAME: workshop
  SPRING_DATASOURCE_URL: "${datasource_url}"
  SPRING_JPA_HIBERNATE_DDL_AUTO: update
  SPRING_JPA_SHOW_SQL: "false"
  JWT_EXPIRATION: "86400000"
  AWS_REGION: "${aws_region}"
  # Fila de entrada do os-orchestrator, montada a partir da branch pela pipeline.
  # Nome determinístico — nunca lido do remote state do outro repositório.
  AWS_SQS_ORCHESTRATOR_QUEUE_URL: "${orchestrator_queue_url}"
  # Fila de entrada da saga (comandos do os-orchestrator): liga o listener
  # OrderEventsSqsListener. A policy os-management-<env>-sqs-access precisa estar
  # anexada à role dos nodes (ver infra/terraform/README.md).
  AWS_SQS_ORDER_EVENTS_ENABLED: "true"
  AWS_SQS_ORDER_EVENTS_QUEUE: "${order_events_queue}"
  # Datadog unified service tagging + Micrometer export. DD_ENV is the branch
  # name (develop/main), substituted by the deploy pipeline.
  DD_ENV: "${datadog_env}"
  DD_SERVICE: "os-management"
  DD_VERSION: "1.0.0"
  DD_METRICS_ENABLED: "true"
  DD_METRICS_URI: "https://api.datadoghq.eu"

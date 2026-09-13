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
  # Datadog unified service tagging + Micrometer export. DD_ENV is the branch
  # name (develop/main), substituted by the deploy pipeline.
  DD_ENV: "${datadog_env}"
  DD_SERVICE: "os-management"
  DD_VERSION: "1.0.0"
  DD_METRICS_ENABLED: "true"
  DD_METRICS_URI: "https://api.datadoghq.com"

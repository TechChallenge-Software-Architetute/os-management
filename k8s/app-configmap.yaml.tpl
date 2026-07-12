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

apiVersion: v1
kind: Secret
metadata:
  name: postgres-secret
  namespace: os-management
type: Opaque
stringData:
  POSTGRES_PASSWORD: "${db_password}"

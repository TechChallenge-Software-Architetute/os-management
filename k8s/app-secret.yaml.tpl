apiVersion: v1
kind: Secret
metadata:
  name: os-management-secret
  namespace: os-management
type: Opaque
stringData:
  SPRING_DATASOURCE_USERNAME: "${db_user}"
  SPRING_DATASOURCE_PASSWORD: "${db_password}"
  JWT_SECRET: "${jwt_secret}"
  EXTERNAL_SERVICE_TOKEN: "${external_token}"
  DD_API_KEY: "${dd_api_key}"

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
  # Credenciais AWS do pod (SQS da saga e SNS). O cluster nao tem IRSA/OIDC
  # (os-management-k8s-terraform nao habilita), entao o pod nao consegue usar
  # role propria: recebe as mesmas chaves do pipeline, como o os-orchestrator.
  # Nenhum valor e commitado aqui; a pipeline substitui a partir dos GitHub Secrets.
  # PENDENCIA: com enable_irsa no EKS, trocar por service-account role.
  AWS_ACCESS_KEY_ID: "${aws_access_key_id}"
  AWS_SECRET_ACCESS_KEY: "${aws_secret_access_key}"
  AWS_SESSION_TOKEN: "${aws_session_token}"

#!/bin/bash
set -e

# Atualiza pacotes e instala Docker
apt-get update -y
apt-get install -y docker.io
systemctl start docker
systemctl enable docker

# Aguarda o RDS aceitar conexoes (pode demorar alguns segundos apos o Terraform aplicar)
sleep 30

# Sobe o container da aplicacao
docker run -d \
  --name os-management \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="${db_url}" \
  -e SPRING_DATASOURCE_USERNAME="${db_user}" \
  -e SPRING_DATASOURCE_PASSWORD="${db_password}" \
  -e JWT_SECRET="${jwt_secret}" \
  -e JWT_EXPIRATION="86400000" \
  -e SERVER_PORT="8080" \
  -e SPRING_APPLICATION_NAME="workshop" \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO="update" \
  -e SPRING_JPA_SHOW_SQL="false" \
  ${app_image}

# Log para debug
echo "Container iniciado: $(docker ps --format '{{.Names}} {{.Status}}')" >> /var/log/os-management.log

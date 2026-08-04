# Infraestrutura — OS Management

Guia completo de configuração de conexões, teste local com Kind e deploy via GitHub Actions + AWS.

---

## O que o Terraform gerencia

```
k8s/
  namespace.yaml                  ← Namespace os-management
  app-configmap.yaml.tpl          ← ConfigMap com SPRING_DATASOURCE_URL dinâmica  ← TF
  postgres-configmap.yaml.tpl     ← ConfigMap do postgres (local only)            ← TF
  app-secret.yaml.tpl             ← Secret com credenciais da app                 ← TF
  postgres-secret.yaml.tpl        ← Secret com senha do postgres (local only)     ← TF
  postgres.yaml                   ← PVC + Deployment + Service postgres (local)    ← TF
  app.yaml                        ← Deployment + Service da aplicação              ← TF
  hpa.yaml                        ← HorizontalPodAutoscaler                        ← TF
  ingress.yaml                    ← Ingress nginx                                  ← TF
  configmap.yaml / secret.yaml    ← Usados pelo time via kustomize (kubectl apply -k k8s/)
  kustomization.yaml              ← Kustomize do time — NÃO usado pelo Terraform
```

**Regra simples:**
- `use_aws = false` → Kind local + postgres in-cluster
- `use_aws = true` → EKS + RDS PostgreSQL gerenciado (postgres.yaml não é aplicado)

---

## 1. O que você precisa alterar para as conexões funcionarem

### 1.1 Credenciais (nunca hardcode — use variáveis)

Copie o arquivo de exemplo e preencha:

```bash
cp terraform/terraform.tfvars.example terraform/terraform.tfvars
```

Edite `terraform/terraform.tfvars`:

```hcl
# Obrigatório sempre
db_user     = "sua_senha_aqui"
db_password = "sua_senha_aqui"
jwt_secret  = "chave-de-pelo-menos-32-caracteres"
github_token = "ghp_SEU_TOKEN"

# Modo local (use_aws = false)
kubernetes_host           = "https://127.0.0.1:PORTA"  # obtido abaixo
kubernetes_token          = "TOKEN_DO_CLUSTER"          # obtido abaixo
kubernetes_ca_certificate = "BASE64_DO_CA"              # obtido abaixo
kube_config               = "KUBECONFIG_EM_BASE64"      # obtido abaixo
```

### 1.2 application.yml — variáveis de ambiente corretas

O arquivo `src/main/resources/application.yml` já está correto com:

```yaml
datasource:
  url:      ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/workshop}
  username: ${SPRING_DATASOURCE_USERNAME:user}
  password: ${SPRING_DATASOURCE_PASSWORD:password}
```

Onde essas variáveis são injetadas:
- **docker-compose**: diretamente no `environment:` do serviço `app`
- **Kubernetes**: via `os-management-secret` (Secret) e `os-management-config` (ConfigMap)
- **Terraform**: popula o Secret e o ConfigMap nos templates `.tpl`

### 1.3 Secrets do GitHub Actions necessários

Acesse: `Settings → Secrets and variables → Actions` no repositório e adicione:

| Secret | Descrição | Obrigatório |
|--------|-----------|-------------|
| `DB_USER` | Usuário do banco | Sempre |
| `DB_PASSWORD` | Senha do banco | Sempre |
| `JWT_SECRET` | Chave JWT | Sempre |
| `AWS_ACCESS_KEY_ID` | Credencial AWS | Só para AWS |
| `AWS_SECRET_ACCESS_KEY` | Credencial AWS | Só para AWS |
| `AWS_REGION` | Ex: `us-east-1` | Só para AWS |

> `GITHUB_TOKEN` é gerado automaticamente pelo GitHub — não precisa criar.

---

## 2. Rodando local com Kind

### Pré-requisitos

```bash
# macOS
brew install kind kubectl terraform

# Linux
curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.23.0/kind-linux-amd64 && chmod +x ./kind && mv ./kind /usr/local/bin/
# terraform: https://developer.hashicorp.com/terraform/downloads
# kubectl: https://kubernetes.io/docs/tasks/tools/install-kubectl-linux/
```

### Passo a passo

**1. Criar o cluster Kind**

```bash
kind create cluster --name os-management
```

**2. Verificar que o cluster está funcionando**

```bash
kubectl cluster-info --context kind-os-management
kubectl get nodes
```

**3. Instalar o ingress-nginx (necessário para o Ingress funcionar)**

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s
```

**4. Exportar as credenciais do cluster**

```bash
# Host da API
export KUBE_HOST=$(kubectl config view --minify -o jsonpath='{.clusters[0].cluster.server}')

# Certificado CA (base64)
export KUBE_CA=$(kubectl config view --minify --raw \
  -o jsonpath='{.clusters[0].cluster.certificate-authority-data}')

# Token (cria um ServiceAccount com permissões de cluster-admin)
kubectl create serviceaccount terraform-sa -n kube-system 2>/dev/null || true
kubectl create clusterrolebinding terraform-sa-admin \
  --clusterrole=cluster-admin \
  --serviceaccount=kube-system:terraform-sa 2>/dev/null || true
export KUBE_TOKEN=$(kubectl create token terraform-sa -n kube-system --duration=24h)

# kubeconfig completo em base64
export KUBE_CONFIG=$(kubectl config view --minify --raw | base64 -w 0)

# Verificar
echo "Host: $KUBE_HOST"
echo "Token: ${KUBE_TOKEN:0:30}..."
```

**5. Preencher o terraform.tfvars**

```bash
cat > terraform/terraform.tfvars << EOF
use_aws     = false
db_name     = "workshop"
db_user     = "user"
db_password = "password"
jwt_secret  = "local-jwt-secret-apenas-para-dev-nao-use-em-prod"

github_token    = "ghp_SEU_TOKEN"
github_owner    = "TechChallenge-Software-Architetute"
repository_name = "os-management"

kubernetes_host           = "${KUBE_HOST}"
kubernetes_token          = "${KUBE_TOKEN}"
kubernetes_ca_certificate = "${KUBE_CA}"
kube_config               = "${KUBE_CONFIG}"
EOF
```

**6. Inicializar e aplicar o Terraform**

```bash
cd terraform
terraform init
terraform plan   # revisar o que será criado
terraform apply  # confirmar com "yes"
```

**7. Verificar os recursos criados**

```bash
# Namespace e pods
kubectl get all -n os-management

# Aguardar o postgres subir (pode levar ~30s)
kubectl wait pod -l app=postgres -n os-management \
  --for=condition=ready --timeout=120s

# Aguardar a app subir (pode levar ~60s — Spring Boot demora para iniciar)
kubectl wait pod -l app=os-management -n os-management \
  --for=condition=ready --timeout=180s

# Verificar ConfigMap e Secret foram criados corretamente
kubectl get configmap os-management-config -n os-management -o yaml
kubectl get secret os-management-secret -n os-management -o yaml
```

**8. Acessar a aplicação**

```bash
# Opção A: port-forward direto
kubectl port-forward svc/os-management 8080:8080 -n os-management

# Acessar em http://localhost:8080/v3/api-docs (Swagger)
# ou http://localhost:8080/swagger-ui/index.html
```

Para o ingress funcionar local (opcional):

```bash
# Adicionar ao /etc/hosts
echo "127.0.0.1 os-management.local" | sudo tee -a /etc/hosts
# Acessar em http://os-management.local/v3/api-docs
```

**9. Destruir o ambiente local**

```bash
cd terraform && terraform destroy
kind delete cluster --name os-management
```

---

## 3. Alternativa mais simples para desenvolver — docker-compose

Antes de precisar do Kubernetes, você pode testar tudo localmente com:

```bash
# Build + start
docker-compose up --build

# Swagger em http://localhost:8080/swagger-ui/index.html
# Banco acessível em localhost:5432 (usuário: user, senha: password, db: workshop)

# Parar
docker-compose down
```

---

## 4. Testando via GitHub Actions + AWS (EKS + RDS)

### Pré-requisitos AWS

- Conta AWS com permissões: `EKS`, `EC2`, `VPC`, `RDS`, `IAM`
- AWS CLI configurado: `aws configure`
- Usuário IAM com as políticas: `AmazonEKSFullAccess`, `AmazonRDSFullAccess`, `AmazonVPCFullAccess`, `IAMFullAccess`

### Passo a passo

**1. Configurar secrets no GitHub**

No repositório → `Settings → Secrets and variables → Actions`:

```
DB_USER         = escolha um usuário (ex: osadmin)
DB_PASSWORD     = senha forte (mínimo 8 chars, sem @)
JWT_SECRET      = string aleatória longa (ex: openssl rand -base64 32)
AWS_ACCESS_KEY_ID     = da sua conta AWS
AWS_SECRET_ACCESS_KEY = da sua conta AWS
AWS_REGION      = us-east-1
```

**2. Fazer push para o repositório**

O pipeline detecta automaticamente:
- `AWS_ACCESS_KEY_ID` **preenchido** → executa `terraform-aws` (EKS + RDS)
- `AWS_ACCESS_KEY_ID` **vazio** → executa `terraform-local` (Kind)

```bash
git add .
git commit -m "feat: infra terraform configurada"
git push origin main
```

**3. Acompanhar o pipeline**

Em `Actions` no GitHub, observe a sequência:

```
unit-test → code-analysis → build → publish → docker-build → terraform-aws
```

O job `terraform-aws` vai:
1. Criar VPC + EKS (≈ 15 min na primeira vez)
2. Criar RDS PostgreSQL (≈ 10 min)
3. Aplicar todos os manifests Kubernetes via Terraform
4. Atualizar a imagem do Deployment com `kubectl set image`
5. Aguardar o rollout completar

**4. Verificar o deploy na AWS**

Após o pipeline terminar:

```bash
# Configurar kubectl para o cluster EKS
aws eks update-kubeconfig --name os-management --region us-east-1

# Verificar pods
kubectl get all -n os-management

# Ver IP do LoadBalancer da aplicação
kubectl get svc os-management -n os-management
# Copiar o EXTERNAL-IP e acessar em http://EXTERNAL-IP:8080/swagger-ui/index.html
```

**5. Verificar que o banco está conectado**

```bash
# Ver logs da app (deve aparecer "Hibernate: ..." nas primeiras requisições)
kubectl logs -l app=os-management -n os-management --tail=50

# Verificar ConfigMap tem a URL do RDS (não localhost)
kubectl get configmap os-management-config -n os-management -o jsonpath='{.data.SPRING_DATASOURCE_URL}'
# Deve retornar: jdbc:postgresql://os-management-postgres.XXXX.us-east-1.rds.amazonaws.com:5432/workshop
```

**6. Destruir a infra AWS (evitar custos)**

```bash
cd terraform

# Preencher terraform.tfvars com use_aws = true e credenciais AWS
terraform init
terraform destroy
# Confirmar com "yes"
```

> **Atenção:** EKS + RDS + NAT Gateway custam ~$5-10/dia. Destrua após testes.

---

## 5. Diagrama do fluxo

```
┌─────────────────────────────────────────────────────────┐
│                    GitHub Actions                        │
│                                                          │
│  unit-test → code-analysis → build → publish            │
│                                         ↓               │
│                                    docker-build          │
│                                    (ghcr.io)             │
│                                    ↓          ↓         │
│                         AWS_KEY vazio?   AWS_KEY set?   │
│                              ↓                ↓         │
│                       terraform-local   terraform-aws   │
│                       (Kind efêmero)    (EKS + RDS)     │
│                              ↓                ↓         │
│                       kubectl apply    kubectl apply     │
│                       + set image      + set image       │
└─────────────────────────────────────────────────────────┘

Terraform local (Kind):            Terraform AWS:
  namespace.yaml                     namespace.yaml
  app-configmap.yaml.tpl             app-configmap.yaml.tpl
  postgres-configmap.yaml.tpl           (URL → RDS endpoint)
  app-secret.yaml.tpl                app-secret.yaml.tpl
  postgres-secret.yaml.tpl           postgres.yaml  ← NÃO aplicado
  postgres.yaml  ← aplicado          app.yaml
  app.yaml                           hpa.yaml
  hpa.yaml                           ingress.yaml
  ingress.yaml
```

---

## 6. Troubleshooting rápido

| Problema | Causa provável | Solução |
|----------|---------------|---------|
| Pod da app em `CrashLoopBackOff` | `SPRING_DATASOURCE_URL` errada | `kubectl describe pod -n os-management` e ver logs |
| `Error: provider produced inconsistent result` | terraform apply em cluster que sumiu | `terraform init && terraform apply` novamente |
| `ImagePullBackOff` | Imagem não existe no ghcr.io | Rodar o pipeline do zero para fazer o push |
| Postgres em `Pending` (local) | PVC não provisionado | Kind precisa de storageClass — confirmar `kubectl get sc` |
| `connection refused` no RDS | Security group errado | Verificar que o SG do EKS está liberado na porta 5432 do RDS |
| Pipeline `terraform-local` e `terraform-aws` ambos pulados | Condição de `secrets.AWS_ACCESS_KEY_ID` | GitHub não avalia secrets em `if:` corretamente — ver nota abaixo |

> **Nota sobre `if: secrets.AWS_ACCESS_KEY_ID`**: O GitHub Actions não permite comparar secrets diretamente no `if:`. Se o pipeline pular ambos os jobs, use um **input manual** ou mova a lógica para um step `env: IS_AWS: ${{ secrets.AWS_ACCESS_KEY_ID != '' }}` e cheque a variável de ambiente. Uma alternativa é usar dois branches (ex: `main` → AWS, `develop` → local).

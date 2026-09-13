# Infraestrutura — OS Management

Guia de deploy: teste local com Kind (kubectl + kustomize) e deploy na AWS via
GitHub Actions anexando ao cluster EKS gerenciado por outro repositório.

---

## Modelo de responsabilidade (importante)

A partir do split em múltiplos repositórios, **esta aplicação não é mais dona de
nenhuma infraestrutura**. Não há mais diretório `terraform/` aqui.

| Recurso | Repositório dono |
|---------|------------------|
| VPC + EKS + HPA (cluster `os-management-<env>`) | `os-management-k8s-terraform` |
| RDS PostgreSQL | `os-management-database` |
| Aplicação (imagem, manifests K8s, deploy) | `os-management` (este repo) |

O deploy da app **anexa** ao cluster já existente e lê o endpoint do banco a partir
de um segredo (`DB_URL_<ENV>`), sem `terraform_remote_state` — modelo
"deployment-independent" adotado pelo time (ver `DEPENDENCIES.md`).

---

## Manifests Kubernetes (`k8s/`)

```
k8s/
  namespace.yaml              ← Namespace os-management
  configmap.yaml              ← ConfigMap da app (modo local/kustomize)
  secret.yaml                 ← Secret da app (modo local/kustomize)
  postgres.yaml               ← Postgres in-cluster (SOMENTE local)
  postgres-init-job.yaml      ← Job de carga inicial do schema (SOMENTE local)
  init.sh                     ← Script DDL/DML usado pelo Job (configMapGenerator)
  app.yaml                    ← Deployment + Service da aplicação
  hpa.yaml                    ← HorizontalPodAutoscaler (min 1 / max 6)
  ingress.yaml                ← Ingress nginx
  kustomization.yaml          ← Entrada do modo local (kubectl apply -k k8s/)
  app-configmap.yaml.tpl      ← Template do ConfigMap para o modo AWS (RDS externo)
  app-secret.yaml.tpl         ← Template do Secret para o modo AWS
```

**Regra simples:**
- **Local (Kind)** → `kubectl apply -k k8s/` — usa `postgres.yaml` in-cluster.
- **AWS (EKS)** → renderiza os `.tpl` com o RDS externo; `postgres.yaml` **não** é aplicado.

---

## 1. Configuração do GitHub Actions

Acesse `Settings → Secrets and variables → Actions`. Convenção do projeto:
**tudo sensível é secret**; apenas flags de controle e região são *variables*.

### 1.1 Variables (repositório)

| Variable | Descrição |
|----------|-----------|
| `USE_AWS` | `true` = deploy no EKS · qualquer outro valor = Kind local. Usado em `if:` de job (por isso é variable, não secret). |

### 1.2 Secrets por ambiente (GitHub Environments `develop` / `main`)

| Secret | Descrição |
|--------|-----------|
| `DB_USER` | Usuário do banco (mesmo valor do `os-management-database`) |
| `DB_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Chave JWT HS256 (idêntica à do `os-management-lambda`) |
| `DOCKER_USERNAME` | Usuário do Docker Hub |
| `DOCKER_HUB_TOKEN` | Token de push do Docker Hub |
| `AWS_REGION` | Ex.: `us-east-1` (com fallback para `us-east-1`) |

### 1.3 Secrets de repositório sufixados por branch (`_DEVELOP` / `_MAIN`)

Escolhidos por branch (`develop` → `_DEVELOP`, `main` → `_MAIN`):

| Secret | Escopo | Descrição |
|--------|--------|-----------|
| `DB_URL_<ENV>` | Repositório | JDBC URL do RDS (output `aurora_jdbc_url` do `os-management-database`) |
| `AWS_ACCESS_KEY_ID_<ENV>` / `AWS_SECRET_ACCESS_KEY_<ENV>` | Organização | Credenciais AWS por conta |

> O nome do cluster **não** é um segredo: é derivado como `os-management-<branch>`
> (`os-management-develop` / `os-management-main`), o mesmo que o
> `os-management-k8s-terraform` cria.

---

## 2. Rodando local com Kind

### Pré-requisitos

```bash
# macOS
brew install kind kubectl

# Linux
curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.23.0/kind-linux-amd64 && chmod +x ./kind && sudo mv ./kind /usr/local/bin/
# kubectl: https://kubernetes.io/docs/tasks/tools/install-kubectl-linux/
```

### Passo a passo

**1. Criar o cluster Kind**

```bash
kind create cluster --name os-management
kubectl cluster-info --context kind-os-management
```

**2. Instalar o ingress-nginx (necessário para o Ingress)**

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s
```

**3. Aplicar os manifests via kustomize**

```bash
kubectl apply -k k8s/
```

Isso cria o namespace, o Postgres in-cluster, o ConfigMap/Secret, o Deployment,
o HPA e o Ingress.

**4. Verificar os recursos**

```bash
kubectl get all -n os-management

kubectl wait pod -l app=postgres -n os-management \
  --for=condition=ready --timeout=120s
kubectl wait pod -l app=os-management -n os-management \
  --for=condition=ready --timeout=180s
```

**5. Acessar a aplicação**

```bash
kubectl port-forward svc/os-management 8080:8080 -n os-management
# http://localhost:8080/swagger-ui/index.html
```

Ingress local (opcional):

```bash
echo "127.0.0.1 os-management.local" | sudo tee -a /etc/hosts
# http://os-management.local/v3/api-docs
```

**6. Destruir o ambiente local**

```bash
kind delete cluster --name os-management
```

---

## 3. Alternativa mais simples para desenvolver — docker-compose

```bash
docker-compose up --build
# Swagger em http://localhost:8080/swagger-ui/index.html
# Banco em localhost:5432 (user/password/workshop)
docker-compose down
```

---

## 4. Deploy na AWS (EKS + RDS) via GitHub Actions

O deploy **não cria** cluster nem banco. Ele assume que:
1. `os-management-k8s-terraform` já criou o cluster `os-management-<branch>`.
2. `os-management-database` já criou o RDS e seu `aurora_jdbc_url` foi copiado
   para o segredo `DB_URL_<ENV>` (ver `DEPENDENCIES.md §5.0.1`).

### Passo a passo

**1. Configurar os secrets/variables** (seções 1.1–1.3).

**2. Ligar o modo AWS**

Defina a variable `USE_AWS = true` no repositório.

**3. Push para `develop` (ou `main`)**

```bash
git push origin develop   # deploy no cluster os-management-develop
git push origin main      # deploy no cluster os-management-main (com gate de aprovação)
```

**4. O job `deploy-aws` executa:**
1. Configura credenciais AWS da conta correta (`_DEVELOP`/`_MAIN`).
2. `aws eks update-kubeconfig --name os-management-<branch>`.
3. Renderiza `app-configmap.yaml.tpl` (URL → RDS) e `app-secret.yaml.tpl`.
4. Aplica namespace/configmap/secret/app/hpa/ingress (sem Postgres in-cluster).
5. `kubectl set image` + aguarda o rollout.

**5. Verificar o deploy**

```bash
aws eks update-kubeconfig --name os-management-develop --region us-east-1
kubectl get all -n os-management
kubectl get svc os-management -n os-management   # hostname do LoadBalancer

kubectl get configmap os-management-config -n os-management \
  -o jsonpath='{.data.SPRING_DATASOURCE_URL}'
# Deve apontar para o endpoint do RDS, não localhost.
```

---

## 5. Diagrama do fluxo

```
┌────────────────────────────────────────────────────────────┐
│                     GitHub Actions                          │
│                                                             │
│  unit-test → code-analysis → build → publish → docker-build │
│                                             ↓        ↓      │
│                                   USE_AWS != true?  USE_AWS==true?
│                                             ↓        ↓      │
│                                     deploy-local   deploy-aws
│                                     (Kind efêmero)  (anexa ao EKS
│                                             ↓        os-management-<branch>)
│                                     kubectl apply    ↓       │
│                                     -k k8s/         update-kubeconfig
│                                     + set image      + kubectl apply (.tpl)
│                                                      + set image        │
└────────────────────────────────────────────────────────────┘

Local (Kind, kustomize):        AWS (EKS, RDS externo):
  namespace.yaml                  namespace.yaml
  configmap.yaml                  app-configmap.yaml.tpl (URL → RDS)
  secret.yaml                     app-secret.yaml.tpl
  postgres.yaml  ← aplicado       postgres.yaml  ← NÃO aplicado
  postgres-init-job.yaml          app.yaml
  app.yaml                        hpa.yaml
  hpa.yaml                        ingress.yaml
  ingress.yaml
```

---

## 6. Troubleshooting rápido

| Problema | Causa provável | Solução |
|----------|---------------|---------|
| Pod da app em `CrashLoopBackOff` | `SPRING_DATASOURCE_URL` errada | `kubectl logs -l app=os-management -n os-management` |
| `deploy-aws` pulado | `USE_AWS` != `true` | Defina a variable `USE_AWS=true` |
| `update-kubeconfig` falha | Cluster `os-management-<branch>` não existe | Rode o pipeline do `os-management-k8s-terraform` primeiro |
| `connection refused` no RDS | SG do RDS não libera os nodes do EKS | Ajuste `VPC_SECURITY_GROUP_IDS_<ENV>` no `os-management-database`/lambda |
| `DB_URL_<ENV>` ausente | Output do banco não sincronizado | Copie `aurora_jdbc_url` para o segredo (`DEPENDENCIES.md §5.0.1`) |
| `ImagePullBackOff` | Imagem não publicada | Verifique o job `docker-build-push` |
| Postgres em `Pending` (local) | Sem storageClass no Kind | `kubectl get sc` — Kind provê `standard` por padrão |

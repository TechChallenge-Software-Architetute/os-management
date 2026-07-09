# Infraestrutura como Código — OS Management

Este diretório contém os scripts Terraform que provisionam toda a infraestrutura necessária para rodar o **OS Management** em um cluster Kubernetes, incluindo banco de dados PostgreSQL, configurações da aplicação e secrets do CI/CD no GitHub.

---

## Estrutura de Arquivos

```
terraform/
├── main.tf                    # Módulo raiz: orquestra todos os sub-módulos
├── variables.tf               # Declaração de todas as variáveis de entrada
├── outputs.tf                 # Valores exportados após o apply
├── terraform.tfvars.example   # Exemplo de arquivo de variáveis (copie e edite)
├── .gitignore                 # Exclui terraform.tfvars e state de versionamento
└── modules/
    ├── database/              # Módulo: PostgreSQL no Kubernetes
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    ├── kubernetes/            # Módulo: Deployment da aplicação
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    └── github/                # Módulo: Secrets do GitHub Actions
        ├── main.tf
        ├── variables.tf
        └── outputs.tf
```

---

## Recursos Criados

### Módulo `database` — PostgreSQL no Kubernetes

| Recurso | Tipo Kubernetes | Descrição |
|---|---|---|
| `postgres-secret` | `Secret` | Credenciais do banco (`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`) |
| `postgres-pvc` | `PersistentVolumeClaim` | Volume de 5 Gi para persistência dos dados |
| `postgres` | `StatefulSet` | Pod do PostgreSQL 16 Alpine com liveness/readiness probes |
| `postgres` | `Service (ClusterIP)` | DNS interno `postgres.<namespace>.svc.cluster.local:5432` |

> **Nota:** Para produção, substitua este módulo por um serviço gerenciado (AWS RDS, Google Cloud SQL, Azure Database for PostgreSQL).

---

### Módulo `kubernetes` — Aplicação OS Management

| Recurso | Tipo Kubernetes | Descrição |
|---|---|---|
| `<app>` | `Namespace` | Namespace isolado para todos os recursos da aplicação |
| `<app>-config` | `ConfigMap` | `SPRING_DATASOURCE_URL`, `LOG_LEVEL`, `JAVA_OPTS` |
| `<app>-secret` | `Secret` | `SPRING_DATASOURCE_USERNAME/PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION` |
| `<app>` | `Deployment` | 2 réplicas da aplicação Spring Boot com rolling update |
| `<app>-service` | `Service` | Expõe a aplicação na porta 80 → 8080 |
| `<app>-pdb` | `PodDisruptionBudget` | Garante mínimo de 1 pod durante manutenções |
| `<app>-hpa` | `HorizontalPodAutoscaler` | Escala de `replicas` até `replicas × 3` com CPU alvo de 70% |

---

### Módulo `github` — Secrets do CI/CD

| Secret GitHub | Descrição |
|---|---|
| `DB_URL` | JDBC URL do banco para o job de migrations Flyway |
| `DB_USER` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |
| `KUBE_CONFIG` | kubeconfig base64 usado pelo `kubectl` no pipeline |

---

## Pré-requisitos

### Ferramentas necessárias

```bash
# Terraform >= 1.0
terraform -version

# kubectl configurado apontando para seu cluster
kubectl cluster-info

# Kind (para cluster local) — opcional
kind version
```

### Cluster local com Kind

Caso não tenha um cluster, crie um com Kind:

```bash
# Instalar Kind
curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.23.0/kind-linux-amd64
chmod +x ./kind && sudo mv ./kind /usr/local/bin/kind

# Criar cluster
kind create cluster --name os-management

# Verificar
kubectl cluster-info --context kind-os-management
```

> **Service type LoadBalancer com Kind:** Kind não provisiona LoadBalancer nativamente.
> Use `service_type = "NodePort"` no `terraform.tfvars` ou instale o MetalLB:
> ```bash
> kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.14.5/config/manifests/metallb-native.yaml
> ```

---

## Como Aplicar

### 1. Configurar variáveis

```bash
cd terraform/
cp terraform.tfvars.example terraform.tfvars
# Edite terraform.tfvars com seus valores reais
```

### 2. Obter credenciais do cluster (Kind)

```bash
# Host do cluster
kubectl config view --raw -o jsonpath='{.clusters[0].cluster.server}'

# CA Certificate (base64)
kubectl config view --raw -o jsonpath='{.clusters[0].cluster.certificate-authority-data}'

# Service Account Token
kubectl create namespace os-management --dry-run=client -o yaml | kubectl apply -f -
kubectl create serviceaccount terraform -n os-management
kubectl create clusterrolebinding terraform-admin \
  --clusterrole=cluster-admin \
  --serviceaccount=os-management:terraform
kubectl create token terraform -n os-management --duration=8760h

# kubeconfig completo (base64)
cat ~/.kube/config | base64 -w0
```

### 3. Inicializar e aplicar

```bash
cd terraform/

# Baixar providers
terraform init

# Visualizar o plano de execução (nada é criado ainda)
terraform plan

# Aplicar a infraestrutura
terraform apply
```

### 4. Verificar os recursos

```bash
# Namespace e pods
kubectl get all -n os-management

# Verificar banco de dados
kubectl get statefulset,pvc -n os-management

# Logs da aplicação
kubectl logs -l app=os-management -n os-management

# IP externo do serviço (pode demorar alguns minutos)
kubectl get svc os-management-service -n os-management
```

---

## Destruir a Infraestrutura

```bash
terraform destroy
```

> **Atenção:** O PVC do PostgreSQL é destruído junto. Para preservar os dados, remova o recurso do state antes de destruir:
> ```bash
> terraform state rm module.database.kubernetes_persistent_volume_claim.postgres_pvc
> ```

---

## Variáveis Principais

| Variável | Padrão | Descrição |
|---|---|---|
| `kubernetes_namespace` | `default` | Namespace dos recursos |
| `kubernetes_replicas` | `2` | Réplicas do pod da aplicação |
| `service_type` | `LoadBalancer` | Tipo do Service K8s |
| `docker_image_tag` | `latest` | Tag da imagem Docker |
| `db_name` | `workshop` | Nome do banco de dados |
| `db_port` | `5432` | Porta do PostgreSQL |
| `jwt_expiration` | `86400000` | Expiração do JWT (ms) |
| `environment` | `dev` | Ambiente (`dev`, `staging`, `prod`) |

Consulte `variables.tf` para a lista completa com descrições e validações.

---

## Outputs Disponíveis Após o Apply

```bash
terraform output kubernetes_namespace          # Namespace criado
terraform output kubernetes_deployment_name   # Nome do Deployment
terraform output kubernetes_service_name      # Nome do Service
terraform output kubernetes_service_external_ip  # IP externo (LoadBalancer)
terraform output github_secrets_created       # Lista de secrets criados no GitHub
```

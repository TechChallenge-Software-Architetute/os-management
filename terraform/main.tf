terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    github = {
      source  = "integrations/github"
      version = "~> 6.0"
    }
    kubectl = {
      source  = "gavinbunney/kubectl"
      version = "~> 1.14"
    }
  }

  # Backend S3 para estado remoto — ativado pelo pipeline via arquivo de config.
  # Local: state fica em terraform.tfstate (sem necessidade de bucket).
  # Pipeline EKS: gera backend.tf dinamicamente antes do init.
  #
  # Para uso manual com S3, crie um arquivo backend.tf:
  #   terraform { backend "s3" { bucket="seu-bucket" key="eks/terraform.tfstate" region="us-east-1" } }
  # E rode: terraform init
}

# =============================================================================
# Providers
# =============================================================================
provider "aws" {
  region = var.aws_region

  # Quando use_aws = false os módulos EKS/RDS têm count = 0 e o provider nunca
  # é chamado de fato. Mesmo assim o Terraform valida credenciais na inicialização,
  # então usamos credenciais dummy e desabilitamos as validações nesse modo.
  access_key                  = var.use_aws ? null : "local-dummy"
  secret_key                  = var.use_aws ? null : "local-dummy"
  skip_credentials_validation = !var.use_aws
  skip_requesting_account_id  = !var.use_aws
  skip_metadata_api_check     = !var.use_aws

  default_tags {
    tags = {
      Project     = "os-management"
      ManagedBy   = "Terraform"
      Environment = var.environment
    }
  }
}

provider "github" {
  owner = var.github_owner
  token = var.github_token
}

# O provider kubectl é usado APENAS no modo local (use_aws = false).
# No modo AWS (use_aws = true), os manifests K8s são aplicados pelo pipeline
# via kubectl direto, após o Terraform criar o EKS + RDS.
provider "kubectl" {
  host                   = var.kubernetes_host
  token                  = var.kubernetes_token
  cluster_ca_certificate = var.kubernetes_ca_certificate != "" ? base64decode(var.kubernetes_ca_certificate) : ""
  load_config_file       = false
}

# =============================================================================
# GitHub Secrets — credenciais injetadas no pipeline CI/CD
# =============================================================================
module "github_secrets" {
  count  = var.github_token != "" ? 1 : 0
  source = "./modules/github"

  repository  = var.repository_name
  db_url      = var.use_aws ? module.rds[0].jdbc_url : "jdbc:postgresql://postgres:15432/${var.db_name}"
  db_user     = var.db_user
  db_password = var.db_password
  jwt_secret  = var.jwt_secret
  kube_config = var.kube_config
}

# =============================================================================
# AWS: EKS Cluster + VPC (ativado quando use_aws = true)
# =============================================================================
module "eks" {
  count  = var.use_aws ? 1 : 0
  source = "./modules/eks"

  cluster_name       = var.app_name
  cluster_version    = var.eks_cluster_version
  aws_region         = var.aws_region
  vpc_cidr           = var.vpc_cidr
  availability_zones = var.availability_zones
  private_subnets    = var.private_subnets
  public_subnets     = var.public_subnets
  node_instance_type = var.node_instance_type
  node_min_size      = var.node_min_size
  node_max_size      = var.node_max_size
  node_desired_size  = var.node_desired_size
}

# =============================================================================
# AWS: RDS PostgreSQL gerenciado (ativado quando use_aws = true)
# Substitui o postgres Deployment que roda in-cluster no modo local.
# =============================================================================
module "rds" {
  count  = var.use_aws ? 1 : 0
  source = "./modules/rds"

  db_identifier             = "${var.app_name}-postgres"
  db_name                   = var.db_name
  db_user                   = var.db_user
  db_password               = var.db_password
  instance_class            = var.rds_instance_class
  allocated_storage         = var.rds_allocated_storage
  multi_az                  = var.rds_multi_az
  skip_final_snapshot       = var.environment != "prod"
  deletion_protection       = var.environment == "prod"
  vpc_id                    = module.eks[0].vpc_id
  subnet_ids                = module.eks[0].private_subnet_ids
  allowed_security_group_id = module.eks[0].node_security_group_id

  depends_on = [module.eks]
}

# =============================================================================
# Kubernetes Manifests — APENAS modo local (use_aws = false)
#
# No modo AWS (use_aws = true), os manifests são aplicados pelo pipeline via
# kubectl após o EKS estar ativo. Isso evita o problema de o provider kubectl
# não conseguir se autenticar antes do cluster existir.
# =============================================================================
locals {
  k8s_path = "${path.module}/../k8s"
  app_manifests = {
    for manifest in split("\n---\n", trimspace(templatefile("${path.module}/../k8s/base/app.yaml", {
      datadog_env = var.datadog_environment
    }))) : sha1(manifest) => manifest
  }
}

# --- Namespace (deve ser o primeiro) ---
resource "kubectl_manifest" "namespace" {
  count     = var.use_aws ? 0 : 1
  yaml_body = file("${local.k8s_path}/namespace.yaml")
}

# --- ConfigMap da aplicação (SPRING_DATASOURCE_URL dinâmica) ---
resource "kubectl_manifest" "app_configmap" {
  count = var.use_aws ? 0 : 1
  yaml_body = templatefile("${local.k8s_path}/app-configmap.yaml.tpl", {
    # Kubernetes Service exposes PostgreSQL on 15432 (targeting container 5432).
    datasource_url          = "jdbc:postgresql://postgres:15432/${var.db_name}"
    datadog_metrics_enabled = var.datadog_metrics_enabled
  })
  depends_on = [kubectl_manifest.namespace]
}

# --- ConfigMap do Postgres (apenas modo local) ---
resource "kubectl_manifest" "postgres_configmap" {
  count = var.use_aws ? 0 : 1
  yaml_body = templatefile("${local.k8s_path}/postgres-configmap.yaml.tpl", {
    db_name = var.db_name
    db_user = var.db_user
  })
  depends_on = [kubectl_manifest.namespace]
}

# --- Secret da aplicação (credenciais nunca hardcoded) ---
resource "kubectl_manifest" "app_secret" {
  count = var.use_aws ? 0 : 1
  yaml_body = templatefile("${local.k8s_path}/app-secret.yaml.tpl", {
    db_user         = var.db_user
    db_password     = var.db_password
    jwt_secret      = var.jwt_secret
    external_token  = "change-me"
    datadog_api_key = var.datadog_api_key
  })
  depends_on = [kubectl_manifest.namespace]
}

# --- Secret do Postgres (apenas modo local) ---
resource "kubectl_manifest" "postgres_secret" {
  count = var.use_aws ? 0 : 1
  yaml_body = templatefile("${local.k8s_path}/postgres-secret.yaml.tpl", {
    db_password = var.db_password
  })
  depends_on = [kubectl_manifest.namespace]
}

# The kustomize workflow creates this ConfigMap via configMapGenerator. Terraform
# applies manifests directly, so it must create the same initialization script.
resource "kubectl_manifest" "postgres_init" {
  count = var.use_aws ? 0 : 1
  yaml_body = yamlencode({
    apiVersion = "v1"
    kind       = "ConfigMap"
    metadata = {
      name      = "postgres-init"
      namespace = "os-management"
    }
    data = {
      "init.sh" = file("${local.k8s_path}/init.sh")
    }
  })
  depends_on = [kubectl_manifest.namespace]
}

# --- Postgres in-cluster (apenas modo local) ---
data "kubectl_path_documents" "postgres" {
  pattern = "${local.k8s_path}/postgres.yaml"
}

resource "kubectl_manifest" "postgres" {
  for_each         = var.use_aws ? {} : data.kubectl_path_documents.postgres.manifests
  yaml_body        = each.value
  wait_for_rollout = false

  depends_on = [
    kubectl_manifest.postgres_configmap,
    kubectl_manifest.postgres_secret,
    kubectl_manifest.postgres_init,
  ]
}

# --- Aplicação principal (apenas modo local) ---
resource "kubectl_manifest" "app" {
  for_each         = var.use_aws ? {} : local.app_manifests
  yaml_body        = each.value
  wait_for_rollout = false

  depends_on = [
    kubectl_manifest.app_configmap,
    kubectl_manifest.app_secret,
    kubectl_manifest.postgres,
  ]
}

# --- HPA (apenas modo local) ---
resource "kubectl_manifest" "hpa" {
  count      = var.use_aws ? 0 : 1
  yaml_body  = file("${local.k8s_path}/hpa.yaml")
  depends_on = [kubectl_manifest.app]
}

# --- Ingress (apenas modo local) ---
resource "kubectl_manifest" "ingress" {
  count      = var.use_aws ? 0 : 1
  yaml_body  = file("${local.k8s_path}/ingress.yaml")
  depends_on = [kubectl_manifest.namespace]
}

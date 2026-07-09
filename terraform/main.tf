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

  # Backend S3 para estado remoto — necessário em CI/CD e trabalho em equipe.
  # Para uso local inicial, comente este bloco (estado fica em terraform.tfstate local).
  # Ative após criar o bucket e a tabela DynamoDB uma vez manualmente.
  #
  # backend "s3" {
  #   bucket         = "os-management-terraform-state"
  #   key            = "os-management/terraform.tfstate"
  #   region         = "us-east-1"
  #   dynamodb_table = "os-management-terraform-locks"
  #   encrypt        = true
  # }
}

# =============================================================================
# Providers
# =============================================================================
provider "aws" {
  region = var.aws_region

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

# O provider kubectl conecta ao cluster.
# use_aws = false → usa variáveis kubernetes_* passadas manualmente (Kind local).
# use_aws = true  → lê endpoint/token/CA diretamente do módulo EKS.
provider "kubectl" {
  host = var.use_aws ? module.eks[0].cluster_endpoint : var.kubernetes_host
  token = var.use_aws ? module.eks[0].cluster_token : var.kubernetes_token
  cluster_ca_certificate = base64decode(
    var.use_aws ? module.eks[0].cluster_ca_certificate : var.kubernetes_ca_certificate
  )
  load_config_file = false
}

# =============================================================================
# GitHub Secrets — credenciais injetadas no pipeline CI/CD
# =============================================================================
module "github_secrets" {
  source = "./modules/github"

  repository  = var.repository_name
  db_url      = var.use_aws ? module.rds[0].jdbc_url : "jdbc:postgresql://${var.db_host}:${var.db_port}/${var.db_name}"
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
# Manifests Kubernetes — padrão do professor (fileset + kubectl_manifest)
# Nota: terraform/ está na raiz do projeto, portanto "../k8s/manifests" é correto.
# =============================================================================
locals {
  manifests_path = "${path.module}/../k8s/manifests"

  namespace_files = fileset("${local.manifests_path}/00-namespaces", "*.yaml")
  config_files    = fileset("${local.manifests_path}/01-config", "*.yaml")

  # No modo AWS, exclui os manifests do postgres in-cluster (substituído pelo RDS)
  app_files = var.use_aws ? {
    for f in fileset("${local.manifests_path}/02-app", "*.yaml") :
    f => f if !startswith(f, "postgres-")
  } : { for f in fileset("${local.manifests_path}/02-app", "*.yaml") : f => f }
}

# 00 — Namespaces (aplicados primeiro)
resource "kubectl_manifest" "namespaces" {
  for_each  = local.namespace_files
  yaml_body = file("${local.manifests_path}/00-namespaces/${each.value}")
}

# 01-config — Secrets dinâmicos via templatefile (credenciais não ficam hardcoded)
resource "kubectl_manifest" "app_secret" {
  yaml_body = templatefile("${local.manifests_path}/01-config/app-secret.yaml.tpl", {
    db_user_b64        = base64encode(var.db_user)
    db_password_b64    = base64encode(var.db_password)
    jwt_secret_b64     = base64encode(var.jwt_secret)
    jwt_expiration_b64 = base64encode(tostring(var.jwt_expiration))
    external_token_b64 = base64encode("change-me")
  })
  depends_on = [kubectl_manifest.namespaces]
}

resource "kubectl_manifest" "postgres_secret" {
  yaml_body = templatefile("${local.manifests_path}/01-config/postgres-secret.yaml.tpl", {
    db_name_b64     = base64encode(var.db_name)
    db_user_b64     = base64encode(var.db_user)
    db_password_b64 = base64encode(var.db_password)
  })
  depends_on = [kubectl_manifest.namespaces]
}

# ConfigMap — SPRING_DATASOURCE_URL dinâmica: local → postgres in-cluster, AWS → RDS endpoint
resource "kubectl_manifest" "configmap" {
  yaml_body = templatefile("${local.manifests_path}/01-config/configmap.yaml.tpl", {
    datasource_url = var.use_aws ? module.rds[0].jdbc_url : "jdbc:postgresql://${var.db_host}:${var.db_port}/${var.db_name}"
    db_name        = var.db_name
    db_user        = var.db_user
  })
  depends_on = [kubectl_manifest.namespaces]
}

# 01-config — Recursos estáticos (PVC, ServiceAccount)
resource "kubectl_manifest" "config" {
  for_each  = local.config_files
  yaml_body = file("${local.manifests_path}/01-config/${each.value}")

  depends_on = [kubectl_manifest.namespaces]
}

# 02-app — Aplicação e postgres in-cluster (excluído no modo AWS)
resource "kubectl_manifest" "app" {
  for_each  = local.app_files
  yaml_body = file("${local.manifests_path}/02-app/${each.value}")

  depends_on = [
    kubectl_manifest.config,
    kubectl_manifest.configmap,
    kubectl_manifest.app_secret,
    kubectl_manifest.postgres_secret,
    module.rds,
  ]
}

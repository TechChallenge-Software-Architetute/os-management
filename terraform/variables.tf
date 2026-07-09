# =============================================================================
# Modo de deployment
# =============================================================================
variable "use_aws" {
  type        = bool
  description = "true = cria EKS + RDS na AWS | false = usa cluster local (Kind/Minikube)"
  default     = false
}

# =============================================================================
# GitHub
# =============================================================================
variable "github_token" {
  type        = string
  description = "GitHub PAT com escopos: repo + secrets"
  sensitive   = true
}

variable "github_owner" {
  type        = string
  description = "Organização ou usuário dono do repositório"
  default     = "TechChallenge-Software-Architetute"
}

variable "repository_name" {
  type        = string
  description = "Nome do repositório no GitHub"
  default     = "os-management"
}

# =============================================================================
# AWS
# =============================================================================
variable "aws_region" {
  type        = string
  description = "Região AWS onde os recursos serão criados"
  default     = "us-east-1"
}

variable "app_name" {
  type        = string
  description = "Nome base da aplicação (prefixo nos recursos AWS)"
  default     = "os-management"
}

variable "environment" {
  type        = string
  description = "Ambiente: dev | staging | prod"
  default     = "dev"
  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Deve ser dev, staging ou prod."
  }
}

# --- EKS ---
variable "eks_cluster_version" {
  type    = string
  default = "1.30"
}

variable "vpc_cidr" {
  type    = string
  default = "10.0.0.0/16"
}

variable "availability_zones" {
  type    = list(string)
  default = ["us-east-1a", "us-east-1b"]
}

variable "private_subnets" {
  type    = list(string)
  default = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "public_subnets" {
  type    = list(string)
  default = ["10.0.101.0/24", "10.0.102.0/24"]
}

variable "node_instance_type" {
  type    = string
  default = "t3.medium"
}

variable "node_min_size" {
  type    = number
  default = 1
}

variable "node_max_size" {
  type    = number
  default = 3
}

variable "node_desired_size" {
  type    = number
  default = 2
}

# --- RDS ---
variable "rds_instance_class" {
  type        = string
  description = "db.t3.micro para dev | db.t3.medium para prod"
  default     = "db.t3.micro"
}

variable "rds_allocated_storage" {
  type    = number
  default = 20
}

variable "rds_multi_az" {
  type        = bool
  description = "false para dev | true para prod"
  default     = false
}

# =============================================================================
# Kubernetes — obrigatórias quando use_aws = false
# =============================================================================
variable "kubernetes_host" {
  type      = string
  sensitive = true
  default   = ""
}

variable "kubernetes_token" {
  type      = string
  sensitive = true
  default   = ""
}

variable "kubernetes_ca_certificate" {
  type      = string
  sensitive = true
  default   = ""
}

variable "kube_config" {
  type        = string
  description = "kubeconfig completo em base64 (injetado como secret no GitHub Actions)"
  sensitive   = true
  default     = ""
}

# =============================================================================
# Banco de dados
# =============================================================================
variable "db_name" {
  type    = string
  default = "workshop"
}

variable "db_user" {
  type      = string
  sensitive = true
}

variable "db_password" {
  type      = string
  sensitive = true
}

# Usados apenas quando use_aws = false (host do postgres in-cluster)
variable "db_host" {
  type    = string
  default = "postgres.os-management.svc.cluster.local"
}

variable "db_port" {
  type    = number
  default = 5432
}

# =============================================================================
# JWT
# =============================================================================
variable "jwt_secret" {
  type      = string
  sensitive = true
}

variable "jwt_expiration" {
  type    = number
  default = 86400000
}

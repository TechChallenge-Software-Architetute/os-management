terraform {
  required_version = ">= 1.0"
  required_providers {
    github = {
      source  = "integrations/github"
      version = "~> 6.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.0"
    }
  }
}

provider "github" {
  owner = var.github_owner
  token = var.github_token
}

provider "kubernetes" {
  host                   = var.kubernetes_host
  token                  = var.kubernetes_token
  cluster_ca_certificate = base64decode(var.kubernetes_ca_certificate)
}

# GitHub Secrets for CI/CD
module "github_secrets" {
  source = "./modules/github"

  repository       = var.repository_name
  db_url           = var.db_url
  db_user          = var.db_user
  db_password      = var.db_password
  kube_config      = var.kube_config
}

# Kubernetes Resources
module "kubernetes" {
  source = "./modules/kubernetes"

  namespace             = var.kubernetes_namespace
  app_name              = var.app_name
  docker_image          = var.docker_image
  docker_image_tag      = var.docker_image_tag
  replicas              = var.kubernetes_replicas
  container_port        = var.container_port
  service_type          = var.service_type
  db_host               = var.db_host
  db_port               = var.db_port
  db_name               = var.db_name
  db_user               = var.db_user
  db_password           = var.db_password
}

# =============================================================================
# GitHub Secrets
# =============================================================================
output "github_secrets_created" {
  description = "Secrets criados no repositório GitHub"
  value       = module.github_secrets.secrets_created
}

# =============================================================================
# Kubernetes manifests aplicados
# =============================================================================
output "namespaces_applied" {
  description = "Namespaces criados pelo Terraform"
  value       = keys(kubectl_manifest.namespaces)
}

output "app_resources_applied" {
  description = "Manifests de aplicação aplicados"
  value       = keys(kubectl_manifest.app)
}

# =============================================================================
# EKS (apenas quando use_aws = true)
# =============================================================================
output "eks_cluster_name" {
  description = "Nome do cluster EKS"
  value       = var.use_aws ? module.eks[0].cluster_name : null
}

output "eks_cluster_endpoint" {
  description = "Endpoint da API do cluster EKS"
  value       = var.use_aws ? module.eks[0].cluster_endpoint : null
  sensitive   = true
}

# =============================================================================
# RDS (apenas quando use_aws = true)
# =============================================================================
output "rds_endpoint" {
  description = "Endpoint do RDS PostgreSQL"
  value       = var.use_aws ? module.rds[0].db_endpoint : null
  sensitive   = true
}

output "rds_jdbc_url" {
  description = "JDBC URL para o Spring Boot se conectar ao RDS"
  value       = var.use_aws ? module.rds[0].jdbc_url : null
  sensitive   = true
}

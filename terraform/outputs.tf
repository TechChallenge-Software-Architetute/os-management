# =============================================================================
# GitHub Secrets
# =============================================================================
output "github_secrets_created" {
  description = "Secrets criados no repositório GitHub"
  value       = var.github_token != "" ? module.github_secrets[0].secrets_created : []
  sensitive   = true
}

# =============================================================================
# Kubernetes manifests aplicados (apenas modo local)
# =============================================================================
output "namespace_applied" {
  description = "Namespace criado pelo Terraform (modo local)"
  value       = var.use_aws ? null : kubectl_manifest.namespace[0].uid
}

output "app_resources_applied" {
  description = "Manifests de aplicação aplicados (modo local)"
  value       = var.use_aws ? null : keys(kubectl_manifest.app)
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

# =============================================================================
# Rede compartilhada — consumida pelo repositório os-management-lambda
# via terraform_remote_state (subnets + SG dos nodes para a Function acessar o RDS).
# =============================================================================
output "private_subnet_ids" {
  description = "IDs das subnets privadas (nodes EKS + RDS). Consumido pela Lambda de autenticação."
  value       = var.use_aws ? module.eks[0].private_subnet_ids : null
}

output "node_security_group_id" {
  description = "Security Group dos nodes EKS (liberado no RDS na porta 5432). Consumido pela Lambda de autenticação."
  value       = var.use_aws ? module.eks[0].node_security_group_id : null
}

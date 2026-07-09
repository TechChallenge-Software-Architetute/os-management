output "cluster_name" {
  value       = module.eks.cluster_name
  description = "Nome do cluster EKS"
}

output "cluster_endpoint" {
  value       = module.eks.cluster_endpoint
  description = "Endpoint do API Server do EKS"
}

output "cluster_ca_certificate" {
  value       = module.eks.cluster_certificate_authority_data
  description = "Certificado CA do cluster (base64)"
  sensitive   = true
}

output "cluster_token" {
  value       = module.eks.cluster_token
  description = "Token de autenticação no cluster"
  sensitive   = true
}

output "vpc_id" {
  value       = module.vpc.vpc_id
  description = "ID da VPC criada"
}

output "private_subnet_ids" {
  value       = module.vpc.private_subnets
  description = "IDs das subnets privadas (nodes + RDS)"
}

output "node_security_group_id" {
  value       = module.eks.node_security_group_id
  description = "Security Group dos nodes EKS (necessário para regra de acesso ao RDS)"
}

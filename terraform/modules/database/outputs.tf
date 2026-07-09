output "db_service_host" {
  value       = "${kubernetes_service.postgres.metadata[0].name}.${var.namespace}.svc.cluster.local"
  description = "Hostname do PostgreSQL dentro do cluster Kubernetes"
}

output "db_service_port" {
  value       = 5432
  description = "Porta do serviço PostgreSQL"
}

output "db_secret_name" {
  value       = kubernetes_secret.postgres_credentials.metadata[0].name
  description = "Nome do Secret Kubernetes com as credenciais do banco"
}

output "db_pvc_name" {
  value       = kubernetes_persistent_volume_claim.postgres_pvc.metadata[0].name
  description = "Nome do PersistentVolumeClaim do PostgreSQL"
}

output "github_secrets_created" {
  value       = module.github_secrets.secrets_created
  description = "List of GitHub secrets that were created"
}

output "kubernetes_namespace" {
  value       = module.kubernetes.namespace
  description = "Kubernetes namespace"
}

output "kubernetes_deployment_name" {
  value       = module.kubernetes.deployment_name
  description = "Kubernetes deployment name"
}

output "kubernetes_service_name" {
  value       = module.kubernetes.service_name
  description = "Kubernetes service name"
}

output "kubernetes_service_external_ip" {
  value       = module.kubernetes.service_external_ip
  description = "External IP of the Kubernetes service"
}

output "kubernetes_configmap_name" {
  value       = module.kubernetes.configmap_name
  description = "Kubernetes configmap name"
}

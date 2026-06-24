output "namespace" {
  value       = kubernetes_namespace.app.metadata[0].name
  description = "Kubernetes namespace"
}

output "deployment_name" {
  value       = kubernetes_deployment.app.metadata[0].name
  description = "Kubernetes deployment name"
}

output "service_name" {
  value       = kubernetes_service.app.metadata[0].name
  description = "Kubernetes service name"
}

output "service_external_ip" {
  value       = try(kubernetes_service.app.status[0].load_balancer[0].ingress[0].ip, "Pending...")
  description = "External IP of the Kubernetes service"
}

output "configmap_name" {
  value       = kubernetes_config_map.app_config.metadata[0].name
  description = "Kubernetes configmap name"
}

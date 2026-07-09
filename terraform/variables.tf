variable "github_token" {
  type        = string
  description = "GitHub personal access token"
  sensitive   = true
}

variable "github_owner" {
  type        = string
  description = "GitHub organization or user name"
  default     = "TechChallenge-Software-Architetute"
}

variable "repository_name" {
  type        = string
  description = "GitHub repository name"
  default     = "os-management"
}

# Database Variables
variable "db_url" {
  type        = string
  description = "Database URL for migrations"
  sensitive   = true
}

variable "db_user" {
  type        = string
  description = "Database user"
  sensitive   = true
}

variable "db_password" {
  type        = string
  description = "Database password"
  sensitive   = true
}

variable "db_host" {
  type        = string
  description = "Database host"
}

variable "db_port" {
  type        = number
  description = "Database port"
  default     = 5432
}

variable "db_name" {
  type        = string
  description = "Database name"
  default     = "workshop"
}

variable "jwt_secret" {
  type        = string
  description = "JWT signing secret"
  sensitive   = true
}

variable "jwt_expiration" {
  type        = number
  description = "JWT expiration in milliseconds"
  default     = 86400000
}

# Kubernetes Variables
variable "kubernetes_host" {
  type        = string
  description = "Kubernetes API server host"
  sensitive   = true
}

variable "kubernetes_token" {
  type        = string
  description = "Kubernetes API token"
  sensitive   = true
}

variable "kubernetes_ca_certificate" {
  type        = string
  description = "Kubernetes CA certificate (base64 encoded)"
  sensitive   = true
}

variable "kube_config" {
  type        = string
  description = "Kubernetes config file (base64 encoded)"
  sensitive   = true
}

variable "kubernetes_namespace" {
  type        = string
  description = "Kubernetes namespace"
  default     = "default"
}

variable "kubernetes_replicas" {
  type        = number
  description = "Number of pod replicas"
  default     = 2
}

variable "container_port" {
  type        = number
  description = "Container port"
  default     = 8080
}

variable "service_type" {
  type        = string
  description = "Kubernetes service type"
  default     = "LoadBalancer"
  validation {
    condition     = contains(["ClusterIP", "NodePort", "LoadBalancer"], var.service_type)
    error_message = "Service type must be ClusterIP, NodePort, or LoadBalancer."
  }
}

# Application Variables
variable "app_name" {
  type        = string
  description = "Application name"
  default     = "os-management"
}

variable "docker_image" {
  type        = string
  description = "Docker image repository"
  default     = "ghcr.io/TechChallenge-Software-Architetute/os-management"
}

variable "docker_image_tag" {
  type        = string
  description = "Docker image tag"
  default     = "latest"
}

variable "environment" {
  type        = string
  description = "Environment name (dev, staging, prod)"
  default     = "dev"
  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be dev, staging, or prod."
  }
}

variable "tags" {
  type        = map(string)
  description = "Common tags for resources"
  default = {
    Project     = "os-management"
    ManagedBy   = "Terraform"
    Environment = "dev"
  }
}

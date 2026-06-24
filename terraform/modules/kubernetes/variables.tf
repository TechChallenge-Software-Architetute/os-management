variable "namespace" {
  type        = string
  description = "Kubernetes namespace"
  default     = "default"
}

variable "app_name" {
  type        = string
  description = "Application name"
}

variable "docker_image" {
  type        = string
  description = "Docker image repository"
}

variable "docker_image_tag" {
  type        = string
  description = "Docker image tag"
  default     = "latest"
}

variable "replicas" {
  type        = number
  description = "Number of pod replicas"
  default     = 2
  validation {
    condition     = var.replicas > 0 && var.replicas <= 10
    error_message = "Replicas must be between 1 and 10."
  }
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
}

variable "db_host" {
  type        = string
  description = "Database host"
}

variable "db_port" {
  type        = number
  description = "Database port"
  default     = 3306
}

variable "db_name" {
  type        = string
  description = "Database name"
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

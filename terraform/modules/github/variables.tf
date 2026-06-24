variable "repository" {
  type        = string
  description = "GitHub repository name"
}

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

variable "kube_config" {
  type        = string
  description = "Kubernetes config file (base64 encoded)"
  sensitive   = true
}

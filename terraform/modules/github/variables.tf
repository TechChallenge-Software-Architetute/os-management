variable "repository" {
  type        = string
  description = "Nome do repositório no GitHub"
}

variable "db_url" {
  type        = string
  description = "JDBC URL do banco de dados"
  sensitive   = true
}

variable "db_user" {
  type      = string
  sensitive = true
}

variable "db_password" {
  type      = string
  sensitive = true
}

variable "jwt_secret" {
  type      = string
  sensitive = true
}

variable "kube_config" {
  type        = string
  description = "kubeconfig completo em base64"
  sensitive   = true
}

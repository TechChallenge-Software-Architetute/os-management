variable "namespace" {
  type        = string
  description = "Kubernetes namespace onde o PostgreSQL será criado"
}

variable "db_name" {
  type        = string
  description = "Nome do banco de dados"
  default     = "workshop"
}

variable "db_user" {
  type        = string
  description = "Usuário do banco de dados"
  sensitive   = true
}

variable "db_password" {
  type        = string
  description = "Senha do banco de dados"
  sensitive   = true
}

variable "postgres_image" {
  type        = string
  description = "Imagem Docker do PostgreSQL"
  default     = "postgres:16-alpine"
}

variable "storage_size" {
  type        = string
  description = "Tamanho do volume persistente para o banco de dados"
  default     = "5Gi"
}

variable "storage_class" {
  type        = string
  description = "StorageClass do Kubernetes (use 'standard' para Kind/Minikube, 'hostpath' para Docker Desktop)"
  default     = "standard"
}

variable "cpu_request" {
  type        = string
  description = "CPU mínima solicitada pelo PostgreSQL"
  default     = "100m"
}

variable "memory_request" {
  type        = string
  description = "Memória mínima solicitada pelo PostgreSQL"
  default     = "128Mi"
}

variable "cpu_limit" {
  type        = string
  description = "CPU máxima permitida ao PostgreSQL"
  default     = "500m"
}

variable "memory_limit" {
  type        = string
  description = "Memória máxima permitida ao PostgreSQL"
  default     = "512Mi"
}

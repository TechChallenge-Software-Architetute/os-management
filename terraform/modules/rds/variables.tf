variable "db_identifier" {
  type        = string
  description = "Identificador único da instância RDS na AWS"
  default     = "os-management-postgres"
}

variable "db_name" {
  type        = string
  description = "Nome do banco de dados criado na instância"
  default     = "workshop"
}

variable "db_user" {
  type        = string
  description = "Usuário master do banco de dados"
  sensitive   = true
}

variable "db_password" {
  type        = string
  description = "Senha do usuário master"
  sensitive   = true
}

variable "db_port" {
  type        = number
  description = "Porta do PostgreSQL"
  default     = 5432
}

variable "instance_class" {
  type        = string
  description = "Tipo de instância RDS (ex: db.t3.micro para dev, db.t3.medium para prod)"
  default     = "db.t3.micro"
}

variable "allocated_storage" {
  type        = number
  description = "Armazenamento inicial em GB"
  default     = 20
}

variable "max_allocated_storage" {
  type        = number
  description = "Armazenamento máximo com autoscaling em GB (0 = desativado)"
  default     = 100
}

variable "postgres_engine_version" {
  type        = string
  description = "Versão do PostgreSQL no RDS"
  default     = "16.3"
}

variable "multi_az" {
  type        = bool
  description = "Habilitar Multi-AZ para alta disponibilidade (recomendado para prod)"
  default     = false
}

variable "skip_final_snapshot" {
  type        = bool
  description = "Pular snapshot final ao destruir (true para dev, false para prod)"
  default     = true
}

variable "deletion_protection" {
  type        = bool
  description = "Proteção contra delete acidental (false para dev, true para prod)"
  default     = false
}

variable "vpc_id" {
  type        = string
  description = "ID da VPC onde o RDS será criado"
}

variable "subnet_ids" {
  type        = list(string)
  description = "IDs das subnets privadas para o DB Subnet Group"
}

variable "allowed_security_group_id" {
  type        = string
  description = "ID do Security Group dos nodes EKS (autorizado a acessar o RDS)"
}

variable "tags" {
  type        = map(string)
  description = "Tags aplicadas aos recursos RDS"
  default     = {}
}

variable "cluster_name" {
  type        = string
  description = "Nome do cluster EKS"
  default     = "os-management"
}

variable "cluster_version" {
  type        = string
  description = "Versão do Kubernetes no EKS"
  default     = "1.30"
}

variable "aws_region" {
  type        = string
  description = "Região AWS"
  default     = "us-east-1"
}

# VPC
variable "vpc_cidr" {
  type        = string
  description = "CIDR block da VPC"
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  type        = list(string)
  description = "AZs para subnets (mínimo 2 para EKS)"
  default     = ["us-east-1a", "us-east-1b"]
}

variable "private_subnets" {
  type        = list(string)
  description = "CIDRs das subnets privadas (nodes EKS e RDS)"
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "public_subnets" {
  type        = list(string)
  description = "CIDRs das subnets públicas (Load Balancer)"
  default     = ["10.0.101.0/24", "10.0.102.0/24"]
}

# Node Group
variable "node_instance_type" {
  type        = string
  description = "Tipo de instância EC2 dos nodes"
  default     = "t3.micro"
}

variable "node_min_size" {
  type        = number
  description = "Mínimo de nodes no grupo"
  default     = 1
}

variable "node_max_size" {
  type        = number
  description = "Máximo de nodes no grupo"
  default     = 3
}

variable "node_desired_size" {
  type        = number
  description = "Número desejado de nodes"
  default     = 2
}

variable "tags" {
  type        = map(string)
  description = "Tags comuns aplicadas a todos os recursos"
  default     = {}
}

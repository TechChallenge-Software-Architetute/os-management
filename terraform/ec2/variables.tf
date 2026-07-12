variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "db_name" {
  type    = string
  default = "workshop"
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

variable "app_image" {
  type        = string
  description = "Imagem Docker da aplicacao no Docker Hub (ex: meuusuario/os-management:latest)"
}

terraform {
  required_version = ">= 1.0"

  # Backend S3 para persistir o state entre execucoes do GitHub Actions.
  # Configuracao parcial: o nome do bucket e passado via -backend-config.
  # Pipeline: terraform init -backend-config="bucket=$TF_STATE_BUCKET"
  # Local:    terraform init -backend-config="bucket=SEU_BUCKET"
  backend "s3" {
    key    = "ec2/terraform.tfstate"
    region = "us-east-1"
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project   = "os-management"
      ManagedBy = "Terraform"
    }
  }
}

# =============================================================================
# VPC padrão da conta — sem custo adicional
# =============================================================================
data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# =============================================================================
# AMI — Ubuntu 24.04 LTS (amd64)
# =============================================================================
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"]
  }
}

# =============================================================================
# Security Groups
# =============================================================================

# SG da EC2 — acesso HTTP na 8080 e SSH
resource "aws_security_group" "app" {
  name        = "os-management-app-sg"
  description = "Acesso HTTP e SSH para a EC2"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "Aplicacao Spring Boot"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# SG do RDS — aceita conexao somente da EC2
resource "aws_security_group" "rds" {
  name        = "os-management-rds-sg"
  description = "PostgreSQL acessivel apenas pela EC2"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description     = "PostgreSQL da EC2"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# =============================================================================
# RDS PostgreSQL — db.t3.micro (free tier: 750h/mes no 1o ano)
# =============================================================================
resource "aws_db_subnet_group" "default" {
  name       = "os-management-subnet-group"
  subnet_ids = data.aws_subnets.default.ids
}

resource "aws_db_instance" "postgres" {
  identifier        = "os-management-postgres"
  engine            = "postgres"
  engine_version    = "16"
  instance_class    = "db.t3.micro"
  allocated_storage = 20
  storage_type      = "gp2"

  db_name  = var.db_name
  username = var.db_user
  password = var.db_password
  port     = 5432

  db_subnet_group_name   = aws_db_subnet_group.default.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false

  skip_final_snapshot = true
  deletion_protection = false

  # Free tier — sem Multi-AZ, sem Performance Insights
  multi_az                     = false
  performance_insights_enabled = false
  backup_retention_period      = 1
}

# =============================================================================
# IAM Role para a EC2 — permite publicar no SNS sem credenciais hardcoded
# Criada somente quando sns_topic_arn for fornecido
# =============================================================================
resource "aws_iam_role" "ec2_role" {
  count = var.sns_topic_arn != "" ? 1 : 0
  name  = "os-management-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "sns_publish" {
  count = var.sns_topic_arn != "" ? 1 : 0
  name  = "os-management-sns-publish"
  role  = aws_iam_role.ec2_role[0].id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = "sns:Publish"
      Resource = var.sns_topic_arn
    }]
  })
}

resource "aws_iam_instance_profile" "ec2_profile" {
  count = var.sns_topic_arn != "" ? 1 : 0
  name  = "os-management-ec2-profile"
  role  = aws_iam_role.ec2_role[0].name
}

# =============================================================================
# EC2 t3.micro — free tier (750h/mes no 1o ano)
# Instala Docker e sobe o container da aplicacao
# =============================================================================
resource "aws_instance" "app" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = "t3.micro"
  vpc_security_group_ids = [aws_security_group.app.id]
  iam_instance_profile   = var.sns_topic_arn != "" ? aws_iam_instance_profile.ec2_profile[0].name : null

  # Script executado na inicializacao da instancia
  user_data = templatefile("${path.module}/user_data.sh.tpl", {
    db_url        = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/${var.db_name}"
    db_name       = var.db_name
    db_user       = var.db_user
    db_password   = var.db_password
    jwt_secret    = var.jwt_secret
    app_image     = var.app_image
    sns_topic_arn = var.sns_topic_arn
    aws_region    = var.aws_region
  })

  # Aguarda o RDS estar disponivel antes de subir a EC2
  depends_on = [aws_db_instance.postgres]
}

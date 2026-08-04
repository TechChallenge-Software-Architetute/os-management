# =============================================================================
# Módulo: rds
# Provisiona uma instância RDS PostgreSQL gerenciada pela AWS.
# No cenário AWS, substitui o postgres Deployment/StatefulSet que roda
# dentro do cluster (usado apenas no ambiente local/Kind).
# =============================================================================

# DB Subnet Group — define em quais subnets privadas o RDS pode ser criado
resource "aws_db_subnet_group" "postgres" {
  name        = "${var.db_identifier}-subnet-group"
  description = "Subnet group para RDS PostgreSQL do OS Management"
  subnet_ids  = var.subnet_ids

  tags = merge(var.tags, {
    Name = "${var.db_identifier}-subnet-group"
  })
}

# Security Group — controla quem pode acessar o RDS na porta 5432
resource "aws_security_group" "rds" {
  name        = "${var.db_identifier}-sg"
  description = "Permite acesso ao PostgreSQL apenas pelos nodes EKS"
  vpc_id      = var.vpc_id

  # Ingress: libera porta 5432 somente para os nodes EKS
  ingress {
    description     = "PostgreSQL from EKS nodes"
    from_port       = var.db_port
    to_port         = var.db_port
    protocol        = "tcp"
    security_groups = [var.allowed_security_group_id]
  }

  # Egress: sem restrições de saída
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, {
    Name = "${var.db_identifier}-sg"
  })
}

# Parameter Group — configura o PostgreSQL para melhor compatibilidade com o Spring Boot
resource "aws_db_parameter_group" "postgres" {
  name        = "${var.db_identifier}-params"
  family      = "postgres16"
  description = "Parameter group customizado para OS Management"

  parameter {
    name  = "log_connections"
    value = "1"
  }

  parameter {
    name  = "log_disconnections"
    value = "1"
  }

  tags = var.tags
}

# Instância RDS PostgreSQL
resource "aws_db_instance" "postgres" {
  identifier = var.db_identifier

  # Engine
  engine         = "postgres"
  engine_version = var.postgres_engine_version

  # Credenciais
  db_name  = var.db_name
  username = var.db_user
  password = var.db_password
  port     = var.db_port

  # Capacidade
  instance_class        = var.instance_class
  allocated_storage     = var.allocated_storage
  max_allocated_storage = var.max_allocated_storage   # 0 desativa storage autoscaling
  storage_type          = "gp3"
  storage_encrypted     = true   # Criptografia em repouso (obrigatório em prod)

  # Rede
  db_subnet_group_name   = aws_db_subnet_group.postgres.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false   # Nunca expor o banco à internet

  # Alta disponibilidade
  multi_az = var.multi_az

  # Backups automáticos (1 dia — limite do free tier)
  backup_retention_period = 1
  backup_window           = "03:00-04:00"
  maintenance_window      = "Mon:04:00-Mon:05:00"

  # Parâmetros customizados
  parameter_group_name = aws_db_parameter_group.postgres.name

  # Proteção contra exclusão acidental
  deletion_protection       = var.deletion_protection
  skip_final_snapshot       = var.skip_final_snapshot
  final_snapshot_identifier = var.skip_final_snapshot ? null : "${var.db_identifier}-final-snapshot"

  # Performance Insights — desativado no free tier (db.t3.micro não suporta)
  performance_insights_enabled = false

  tags = merge(var.tags, {
    Name = var.db_identifier
  })
}

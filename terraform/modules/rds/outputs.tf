output "db_endpoint" {
  value       = aws_db_instance.postgres.address
  description = "Endpoint (hostname) da instância RDS"
}

output "db_port" {
  value       = aws_db_instance.postgres.port
  description = "Porta do PostgreSQL"
}

output "db_name" {
  value       = aws_db_instance.postgres.db_name
  description = "Nome do banco de dados criado"
}

output "jdbc_url" {
  value       = "jdbc:postgresql://${aws_db_instance.postgres.address}:${aws_db_instance.postgres.port}/${aws_db_instance.postgres.db_name}"
  description = "JDBC URL completa para o Spring Boot (usada no ConfigMap)"
}

output "app_url" {
  description = "URL publica da aplicacao"
  value       = "http://${aws_instance.app.public_ip}:8080/swagger-ui/index.html"
}

output "ec2_public_ip" {
  description = "IP publico da EC2"
  value       = aws_instance.app.public_ip
}

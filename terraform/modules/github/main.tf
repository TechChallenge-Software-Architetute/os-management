resource "github_actions_secret" "db_url" {
  repository  = var.repository
  secret_name = "DB_URL"
  value       = var.db_url
}

resource "github_actions_secret" "db_user" {
  repository  = var.repository
  secret_name = "DB_USER"
  value       = var.db_user
}

resource "github_actions_secret" "db_password" {
  repository  = var.repository
  secret_name = "DB_PASSWORD"
  value       = var.db_password
}

resource "github_actions_secret" "jwt_secret" {
  repository  = var.repository
  secret_name = "JWT_SECRET"
  value       = var.jwt_secret
}

resource "github_actions_secret" "kube_config" {
  repository  = var.repository
  secret_name = "KUBE_CONFIG"
  value       = var.kube_config
}

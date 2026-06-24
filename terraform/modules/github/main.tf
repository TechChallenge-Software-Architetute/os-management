resource "github_actions_secret" "db_url" {
  repository      = var.repository
  secret_name     = "DB_URL"
  plaintext_value = var.db_url
}

resource "github_actions_secret" "db_user" {
  repository      = var.repository
  secret_name     = "DB_USER"
  plaintext_value = var.db_user
}

resource "github_actions_secret" "db_password" {
  repository      = var.repository
  secret_name     = "DB_PASSWORD"
  plaintext_value = var.db_password
}

resource "github_actions_secret" "kube_config" {
  repository      = var.repository
  secret_name     = "KUBE_CONFIG"
  plaintext_value = var.kube_config
}

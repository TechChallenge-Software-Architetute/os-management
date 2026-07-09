output "secrets_created" {
  description = "Nomes dos secrets criados no GitHub Actions"
  value = [
    github_actions_secret.db_url.secret_name,
    github_actions_secret.db_user.secret_name,
    github_actions_secret.db_password.secret_name,
    github_actions_secret.jwt_secret.secret_name,
    github_actions_secret.kube_config.secret_name,
  ]
}

provider "aws" {
  region = var.region

  # Guardrail: fail fast if the active credentials point at an unexpected account.
  # Empty (local validation) = no restriction; CI sets TF_VAR_aws_account_id.
  allowed_account_ids = var.aws_account_id != "" ? [var.aws_account_id] : []

  default_tags {
    tags = {
      Project     = "os-management"
      Component   = "os-management-messaging"
      ManagedBy   = "Terraform"
      Environment = var.environment
    }
  }
}

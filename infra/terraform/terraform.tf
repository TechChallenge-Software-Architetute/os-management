terraform {
  required_version = ">= 1.9.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Configured in CI: -backend-config="key=os-management/<env>/terraform.tfstate"
  backend "s3" {}
}

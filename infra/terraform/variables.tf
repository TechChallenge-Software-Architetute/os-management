variable "region" {
  description = "Região da AWS."
  type        = string
  default     = "us-east-1"
}

variable "aws_account_id" {
  description = "Conta AWS esperada. Protege o provider contra deploy na conta errada. Vazio = sem restrição (validação local)."
  type        = string
  default     = ""
}

variable "environment" {
  description = "Nome do ambiente. Por convenção é o nome da branch (develop/main)."
  type        = string

  validation {
    condition     = can(regex("^[a-z][a-z0-9-]{0,20}$", var.environment))
    error_message = "environment deve ser minúsculo, alfanumérico com hífens, começando por letra."
  }
}

# --- Fila de entrada (resultado da saga) -------------------------------------

variable "queue_visibility_timeout_seconds" {
  description = "Visibility timeout da fila de eventos. Deve ser maior que o pior caso de processamento do listener."
  type        = number
  default     = 60
}

variable "queue_message_retention_seconds" {
  description = "Tempo que uma mensagem não consumida permanece na fila principal (segundos)."
  type        = number
  default     = 345600 # 4 dias
}

variable "dlq_message_retention_seconds" {
  description = "Tempo que uma mensagem permanece na DLQ (segundos)."
  type        = number
  default     = 1209600 # 14 dias
}

variable "queue_max_receive_count" {
  description = "Tentativas de entrega antes de a mensagem ir para a DLQ."
  type        = number
  default     = 5
}

variable "producer_role_arns" {
  description = <<-EOT
    ARNs de roles/users autorizados a sqs:SendMessage na fila de eventos — na
    prática, a role do os-orchestrator.

    Vazio (default) não cria resource policy alguma: produtor na mesma conta é
    autorizado pela própria policy IAM dele, o que mantém este deploy
    independente do deploy do os-orchestrator. Preencha somente para produtores
    em outra conta. Nunca use "*".
  EOT
  type        = list(string)
  default     = []

  validation {
    condition     = !contains(var.producer_role_arns, "*")
    error_message = "producer_role_arns deve nomear principals específicos; um wildcard tornaria a fila gravável por qualquer conta AWS."
  }
}

# --- Permissão da aplicação --------------------------------------------------

variable "create_app_access_policy" {
  description = <<-EOT
    Cria a managed policy IAM que a aplicação precisa para falar com as duas
    filas (consumir a própria, publicar na do orquestrador).

    Esta stack apenas *cria* a policy e expõe o ARN; o attach na role/user que a
    aplicação usa é passo manual, porque a role dos nodes pertence ao stack
    os-management-k8s-terraform e escrever nele aqui criaria a dependência entre
    deploys que a arquitetura proíbe.
  EOT
  type        = bool
  default     = true
}

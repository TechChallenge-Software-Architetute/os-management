# Esta stack cria apenas o que o os-management possui: a fila em que ele recebe o
# resultado da saga, e a DLQ dela. Não há Lambda aqui — a aplicação continua no
# EKS, implantada por `kubectl apply` no job deploy-aws.
#
# Nenhum terraform_remote_state e nenhum recurso criado dentro do stack de outro
# repositório. A fila do os-orchestrator é endereçada por nome determinístico
# (os-<serviço>-<env>-<propósito>), montado a partir da conta e região atuais, de
# modo que esta stack aplica em qualquer ordem — inclusive antes do orquestrador
# existir.

data "aws_caller_identity" "current" {}

locals {
  queue_name = "os-management-${var.environment}-order-events"
  dlq_name   = "os-management-${var.environment}-order-events-dlq"

  # Fila de outro repositório, referenciada por nome — nunca por remote state.
  orchestrator_queue_name = "os-orchestrator-${var.environment}-order-processing"
  orchestrator_queue_arn  = "arn:aws:sqs:${var.region}:${data.aws_caller_identity.current.account_id}:${local.orchestrator_queue_name}"
  orchestrator_queue_url  = "https://sqs.${var.region}.amazonaws.com/${data.aws_caller_identity.current.account_id}/${local.orchestrator_queue_name}"
}

# --- Fila de entrada + DLQ ---------------------------------------------------

resource "aws_sqs_queue" "order_events_dlq" {
  name                      = local.dlq_name
  message_retention_seconds = var.dlq_message_retention_seconds
  sqs_managed_sse_enabled   = true
}

resource "aws_sqs_queue" "order_events" {
  name                       = local.queue_name
  visibility_timeout_seconds = var.queue_visibility_timeout_seconds
  message_retention_seconds  = var.queue_message_retention_seconds
  sqs_managed_sse_enabled    = true
  receive_wait_time_seconds  = 20

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.order_events_dlq.arn
    maxReceiveCount     = var.queue_max_receive_count
  })
}

# Resource policy só para produtor em outra conta; ver producer_role_arns.
data "aws_iam_policy_document" "order_events" {
  count = length(var.producer_role_arns) > 0 ? 1 : 0

  statement {
    sid       = "AllowNamedProducersToSend"
    effect    = "Allow"
    actions   = ["sqs:SendMessage"]
    resources = [aws_sqs_queue.order_events.arn]

    principals {
      type        = "AWS"
      identifiers = var.producer_role_arns
    }
  }

  statement {
    sid       = "DenyInsecureTransport"
    effect    = "Deny"
    actions   = ["sqs:*"]
    resources = [aws_sqs_queue.order_events.arn]

    principals {
      type        = "AWS"
      identifiers = ["*"]
    }

    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values   = ["false"]
    }
  }
}

resource "aws_sqs_queue_policy" "order_events" {
  count = length(var.producer_role_arns) > 0 ? 1 : 0

  queue_url = aws_sqs_queue.order_events.id
  policy    = data.aws_iam_policy_document.order_events[0].json
}

# --- Policy de acesso da aplicação ------------------------------------------
# Menor privilégio: consumir a própria fila e publicar na fila do orquestrador,
# nada mais. A policy é criada aqui mas o attach é manual (ver
# create_app_access_policy), para não escrever na role de outro stack.

data "aws_iam_policy_document" "app_access" {
  count = var.create_app_access_policy ? 1 : 0

  statement {
    sid = "ConsumeOwnOrderEventsQueue"
    actions = [
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
      "sqs:GetQueueUrl",
    ]
    resources = [aws_sqs_queue.order_events.arn]
  }

  statement {
    sid     = "StartSagaOnOrchestratorQueue"
    actions = ["sqs:SendMessage", "sqs:GetQueueUrl"]
    # ARN montado por nome: a fila pode ainda não existir no momento do apply.
    resources = [local.orchestrator_queue_arn]
  }
}

resource "aws_iam_policy" "app_access" {
  count = var.create_app_access_policy ? 1 : 0

  name        = "os-management-${var.environment}-sqs-access"
  description = "Permite ao os-management consumir a fila de eventos e publicar na fila do os-orchestrator."
  policy      = data.aws_iam_policy_document.app_access[0].json
}

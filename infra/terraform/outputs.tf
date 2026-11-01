output "order_events_queue_name" {
  description = "Nome da fila onde o os-management recebe o resultado da saga."
  value       = aws_sqs_queue.order_events.name
}

output "order_events_queue_url" {
  description = "URL da fila de eventos."
  value       = aws_sqs_queue.order_events.url
}

output "order_events_queue_arn" {
  description = "ARN da fila de eventos. O produtor concede sqs:SendMessage sobre este ARN na própria policy IAM."
  value       = aws_sqs_queue.order_events.arn
}

output "order_events_dlq_url" {
  description = "URL da DLQ da fila de eventos."
  value       = aws_sqs_queue.order_events_dlq.url
}

output "orchestrator_queue_url" {
  description = "URL da fila do os-orchestrator, montada por nome determinístico (não lida de remote state). É o valor de AWS_SQS_ORCHESTRATOR_QUEUE_URL no ConfigMap."
  value       = local.orchestrator_queue_url
}

output "app_access_policy_arn" {
  description = "ARN da managed policy de acesso SQS da aplicação. O attach na role/user usada pelos pods é passo manual — ver README de infra."
  value       = var.create_app_access_policy ? aws_iam_policy.app_access[0].arn : ""
}

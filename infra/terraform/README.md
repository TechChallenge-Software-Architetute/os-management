# Infraestrutura de mensageria do os-management

Esta stack cria **apenas o que o os-management possui**:

| Recurso | Nome |
|---|---|
| Fila de entrada (resultado da saga) | `os-management-<env>-order-events` |
| DLQ (redrive, maxReceiveCount 5) | `os-management-<env>-order-events-dlq` |
| Managed policy de acesso SQS da aplicação | `os-management-<env>-sqs-access` |

`<env>` é o nome da branch (`develop`/`main`).

**Não há Lambda aqui.** A aplicação continua no EKS, implantada pelo job
`deploy-aws` do `pipeline.yml` — essa decisão foi mantida porque o
`os-management` segue sendo o roteador HTTP do sistema.

## Deploy independente

Nenhum `terraform_remote_state`. A fila do `os-orchestrator` é referenciada por
nome determinístico (`os-orchestrator-<env>-order-processing`), com o ARN montado
a partir de `aws_caller_identity` e da região. Por isso esta stack aplica em
qualquer ordem, inclusive antes de o orquestrador existir.

Os jobs `terraform-preflight` e `terraform-deploy` não declaram `needs`, então
rodam em paralelo aos jobs da aplicação e não interferem no deploy do Kubernetes.

## Passo manual necessário

A policy `os-management-<env>-sqs-access` é **criada** aqui, mas o attach é
manual:

```bash
# ARN da policy
terraform output -raw app_access_policy_arn

# Role usada pelos nodes do EKS (output do os-management-k8s-terraform)
aws iam attach-role-policy --role-name <node-role> --policy-arn <policy-arn>
```

O attach não é feito pelo Terraform de propósito: a role dos nodes pertence ao
stack `os-management-k8s-terraform`, e escrever nela a partir daqui criaria
exatamente a dependência entre deploys que a arquitetura proíbe.

> **Pendência (F3):** o cluster não tem IRSA — `os-management-k8s-terraform` não
> habilita OIDC. Com IRSA, esta policy seria anexada a uma service-account role
> em vez da role dos nodes, que hoje dá a permissão a **todos** os pods do
> cluster. Habilitar `enable_irsa = true` é o caminho certo.

## Segurança

- SSE-SQS habilitado nas duas filas.
- `producer_role_arns` vazio por padrão: o orquestrador é autorizado pela própria
  policy IAM dele, sem resource policy e sem wildcard. Preencha somente para
  produtor em outra conta.
- A policy da aplicação é de menor privilégio: consumir a própria fila e publicar
  na fila do orquestrador, nada mais.

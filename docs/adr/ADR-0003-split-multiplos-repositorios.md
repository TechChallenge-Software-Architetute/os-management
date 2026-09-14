# ADR-0003: Split em Múltiplos Repositórios com CI/CD Independente

- **Status:** Aceito
- **Data:** 2026-09-13

## Contexto

O requisito obrigatório da Fase 3 pede a organização do projeto em **quatro
repositórios separados**, cada um com CI/CD próprio e deploy automático:
(1) Lambda/Function Serverless, (2) Infraestrutura Kubernetes (Terraform),
(3) Infraestrutura do Banco de Dados Gerenciado (Terraform), (4) Aplicação
principal em Kubernetes. Antes da Fase 3, toda a infraestrutura (Terraform de
EC2/RDS) vivia dentro do próprio repositório da aplicação.

## Decisão

Segregar o projeto em **cinco** repositórios (um a mais do que o mínimo
pedido), cada um com pipeline de CI/CD e ciclo de deploy independentes:

| Repositório | Responsabilidade |
|---|---|
| `os-management` | Aplicação principal (Spring Boot), manifests K8s, deploy no EKS |
| `os-management-k8s-terraform` | VPC + EKS + `metrics-server` (Terraform) |
| `os-management-database` | RDS PostgreSQL (Terraform) + schema (`ddl.sql`/`dml.sql`) |
| `os-management-lambda` | Functions serverless de autenticação (issuer + authorizer) |
| `os-management-gateway` | API Gateway (Terraform) — **repositório adicional**, não exigido explicitamente pelo enunciado, mas necessário para isolar o ciclo de vida do Gateway do ciclo de vida das Lambdas |

Cada repositório tem seu próprio pipeline (GitHub Actions), branch `main`/
`master` protegida contra commit direto, merge apenas via Pull Request, e
deploy automático nas branches de homologação (`develop`) e produção (`main`).
A comunicação entre repositórios de infraestrutura se dá via
`terraform_remote_state` (ex.: `os-management-database` lê `vpc_id` e
`node_security_group_id` do `os-management-k8s-terraform`), nunca por
acoplamento direto de código.

## Consequências

- **Deploys independentes:** um ajuste no schema do banco não exige rebuild
  da aplicação nem do cluster; uma mudança na função Lambda não exige
  reaplicar o Terraform do EKS.
- **Ordem de deploy explícita e documentada** (`k8s-terraform → database →
  lambda → gateway → app`), já que os repositórios de nível mais alto
  dependem de outputs dos repositórios de infraestrutura base — ver
  `os-management-gateway/README.md § Deploy order`.
- **Responsabilidade e superfície de permissões reduzidas por repositório** —
  cada pipeline só tem os secrets/credenciais AWS necessários para seu
  próprio escopo (ex.: o repositório da aplicação não tem permissão para
  alterar o cluster EKS, apenas para fazer deploy de workloads nele).
- **Repositório extra (`gateway`) fora do mínimo pedido:** decisão consciente
  de separar o Gateway das Lambdas para não acoplar o ciclo de vida de
  roteamento/autorização (que muda pouco) ao ciclo de vida das funções de
  autenticação (que evolui com mais frequência). Isso deve ser destacado
  explicitamente na entrega, já que o enunciado fala em "quatro
  repositórios" — o time optou por uma segregação mais fina.
- **Trade-off:** mais repositórios para manter atualizados (5 READMEs, 5
  pipelines, 5 conjuntos de secrets), custo aceito em troca do isolamento de
  responsabilidades.

## Alternativas Rejeitadas

- **Monorepo único com múltiplos pipelines por pasta:** rejeitado por não
  atender ao requisito explícito de "repositórios separados" e por manter
  acoplado o histórico de commits de aplicação e infraestrutura.
- **Gateway dentro do repositório `os-management-lambda`:** avaliado, mas
  rejeitado porque o Gateway é reaplicado com bem menos frequência que as
  Lambdas — misturar os dois no mesmo pipeline forçaria reavaliação
  desnecessária do Gateway a cada mudança na lógica de autenticação.

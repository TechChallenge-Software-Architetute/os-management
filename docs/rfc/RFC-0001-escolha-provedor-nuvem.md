# RFC-0001: Escolha do Provedor de Nuvem

- **Status:** Aceito
- **Data:** 2026-09-13
- **Repositórios afetados:** todos (`os-management`, `os-management-gateway`,
  `os-management-lambda`, `os-management-k8s-terraform`, `os-management-database`)

## Contexto

O Tech Challenge da Fase 3 exige elevar a aplicação a um nível de operação
corporativa: API Gateway, função serverless para autenticação, banco de dados
gerenciado, cluster Kubernetes com escalabilidade e infraestrutura como código —
com escolha livre de nuvem. O time precisava de um provedor que oferecesse
todos esses serviços gerenciados nativamente e que fosse viável para um projeto
acadêmico, sem custo real recorrente para os alunos.

## Alternativas Consideradas

| Alternativa | Prós | Contras |
|---|---|---|
| **AWS (via AWS Academy Learner Lab)** | Acesso gratuito via laboratório acadêmico da FIAP; cobre EKS, RDS, Lambda, API Gateway, SNS, Secrets Manager; documentação e comunidade amplas | Credenciais temporárias (~4h por sessão de lab), sem acesso a alguns serviços (ex.: SES bloqueado no Lab) |
| **Google Cloud Platform (GKE + Cloud SQL + Cloud Functions)** | Bom suporte a Kubernetes gerenciado (GKE) | Sem laboratório acadêmico gratuito equivalente disponível para a turma; custo real via cartão de crédito |
| **Azure (AKS + Azure SQL + Functions)** | Bom suporte a Kubernetes gerenciado (AKS) | Mesma limitação de custo/acesso acadêmico da GCP |
| **On-premise / self-hosted (k3s em VPS)** | Controle total, custo previsível | Sem gerenciamento nativo de banco/serverless/API Gateway; todo o trabalho de HA e patch recai sobre o time |

## Decisão

Adotar a **AWS**, utilizando o **AWS Academy Learner Lab** disponibilizado pela
FIAP como conta de execução.

Principais fatores:

1. **Custo zero para o time** — o Learner Lab fornece crédito e credenciais
   temporárias sem necessidade de cartão de crédito pessoal.
2. **Cobertura completa dos requisitos obrigatórios** em serviços gerenciados
   nativos: EKS (Kubernetes), RDS (banco gerenciado), Lambda (serverless),
   API Gateway, SNS (notificação), Secrets Manager.
3. **Precedente no projeto** — as fases anteriores do Tech Challenge já usavam
   EC2/RDS na AWS; migrar para AWS gerenciado (EKS/RDS) aproveita conhecimento
   acumulado do time e scripts/pipelines já existentes.

## Consequências

- As credenciais do Lab expiram a cada sessão (~4h) e precisam ser
  atualizadas manualmente nos secrets do GitHub Actions e no `.env` local a
  cada rodada de testes — ver [os-management/README.md § Notificação por
  Email](../../README.md#notificacao-por-email-aws-sns).
- Alguns serviços ficam indisponíveis no Lab (ex.: **SES**), o que motivou a
  escolha de **SNS** para notificação por e-mail em vez de SES (decisão
  registrada no próprio README da aplicação).
- A infraestrutura foi desenhada para ser **portável entre contas AWS**
  (dev/prod) via variáveis de ambiente e Terraform, minimizando o acoplamento
  a uma conta específica do Lab, caso o projeto precise migrar para uma conta
  AWS paga no futuro.
- Os 4 repositórios de infraestrutura/aplicação (`os-management-k8s-terraform`,
  `os-management-database`, `os-management-lambda`, `os-management`) e o
  `os-management-gateway` compartilham a mesma conta e região (`us-east-1`),
  simplificando o roteamento de rede entre EKS, RDS e Lambda (ver
  [ADR-0003](../adr/ADR-0003-split-multiplos-repositorios.md)).

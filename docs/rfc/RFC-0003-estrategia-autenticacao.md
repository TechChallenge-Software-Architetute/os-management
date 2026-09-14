# RFC-0003: Estratégia de Autenticação via CPF

- **Status:** Aceito
- **Data:** 2026-09-13
- **Repositórios afetados:** `os-management-gateway`, `os-management-lambda`,
  `os-management`

## Contexto

O requisito obrigatório da Fase 3 pede: um API Gateway protegendo rotas
sensíveis com **autenticação via CPF**, implementada como **função
serverless** que valida o CPF, consulta a existência/status do cliente no
banco e devolve um **JWT**. Isso é um mecanismo de autenticação **novo e
paralelo** ao login existente (e-mail/senha via `POST /auth/login`) usado
pela equipe interna (ADMIN/TECHNICIAN) da oficina — o cliente final não tem
senha cadastrada, apenas o CPF/CNPJ já registrado no cadastro.

## Alternativas Consideradas

| Alternativa | Prós | Contras |
|---|---|---|
| **Function Serverless própria (AWS Lambda) + API Gateway REST + JWT (HS256)** | Atende ao requisito obrigatório ao pé da letra; reaproveita a biblioteca `jjwt` já usada no backend; sem custo de serviço gerenciado de identidade | Time precisa manter duas Lambdas (issuer/authorizer) e o segredo compartilhado (`JWT_SECRET`) |
| **Amazon Cognito (User Pool + Lambda Trigger)** | Serviço gerenciado de identidade, MFA pronto | CPF não é um "usuário" com senha — adaptar o Cognito para autenticar por CPF exigiria um fluxo customizado (custom auth challenge) mais complexo do que o exigido; overhead desnecessário para o escopo do desafio |
| **Autenticação via CPF direto no backend Spring (sem API Gateway/Lambda)** | Mais simples, um único deploy | Não atende ao requisito obrigatório de "Function Serverless" nem de proteção de rotas via **API Gateway**; perderia a segregação exigida entre Gateway/Lambda/App |
| **Kong ou Traefik (self-hosted) como API Gateway** | Open-source, roda no próprio cluster K8s | Exigiria operar e manter mais um componente dentro do EKS; o **AWS API Gateway** gerenciado já resolve roteamento, autorizador customizado e cache de autorização sem operação adicional |

## Decisão

1. **API Gateway:** AWS API Gateway (REST API), gerenciado, no repositório
   `os-management-gateway`.
2. **Autenticação:** duas AWS Lambdas no repositório `os-management-lambda`:
   - **Auth Issuer** (`POST /auth`) — valida dígitos verificadores do CPF,
     consulta `clients.document` no RDS e emite um JWT (HS256) se o cliente
     existir e estiver ativo.
   - **Token Authorizer** — validado como `TOKEN` authorizer do API Gateway
     em toda rota `ANY /{proxy+}`, verifica assinatura/expiração do JWT sem
     acessar o banco.
3. Ambas as Lambdas assinam/validam com o **mesmo `JWT_SECRET`** (HS256),
   compatível com a biblioteca `jjwt` já usada pelo backend principal.

O fluxo completo (requisição → Gateway → Lambda → resposta) está diagramado em
[`os-management-lambda/README.md § Architecture`](../../../os-management-lambda/README.md#architecture)
e o diagrama de sequência da autenticação está consolidado em
[`architecture.md § Diagramas de Sequência`](../architecture.md#diagramas-de-sequência).

## Consequências

- Surgem **dois mecanismos de autenticação coexistindo** no sistema: JWT de
  staff (e-mail/senha, `POST /auth/login`, roles `ADMIN`/`TECHNICIAN`) e JWT
  de cliente (CPF, `POST /auth` via Gateway, role `CLIENT`). O backend
  precisa reconhecer ambos os formatos de token (ver `SecurityConfig` /
  `JwtFilter` no `os-management`, claim `clientId` distingue os tokens de
  cliente).
- O **Token Authorizer** não acessa banco — apenas valida assinatura/expiração
  — o que permite ao API Gateway **cachear o resultado** por 300s
  (`authorizer_result_ttl_in_seconds`) sem custo adicional de leitura no RDS
  a cada chamada.
- Rotas de staff (`/auth/login`, Swagger, endpoints administrativos)
  continuam sendo acessadas **diretamente no backend**, fora do API Gateway —
  o Gateway é a porta de entrada apenas para o fluxo do cliente final.
- Dependência de deploy: o backend precisa estar em uma versão que reconheça
  o token de CPF (`feature/cpf-auth-integration` ou posterior) antes que
  chamadas protegidas via Gateway funcionem de ponta a ponta — documentado
  como pré-requisito no README do gateway.
- `iss`/`aud` ainda não são cravados no JWT — os tokens são distinguidos
  apenas pela presença da claim `clientId`. Registrado como possível
  *hardening* futuro (ver notas do `os-management-lambda/README.md`).

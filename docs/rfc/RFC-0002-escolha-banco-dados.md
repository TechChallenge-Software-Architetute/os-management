# RFC-0002: Escolha do Banco de Dados Gerenciado

- **Status:** Aceito
- **Data:** 2026-09-13
- **Repositórios afetados:** `os-management-database`, `os-management`,
  `os-management-lambda`

## Contexto

O domínio da oficina (clientes, veículos, produtos, estoque, orçamento, ordens
de serviço) tem forte necessidade de **integridade referencial** e
**transações atômicas** — por exemplo, a reserva de estoque é "tudo ou nada"
(ver [Reserva de Estoque](../../../os-tech-documentation/02%20-%20Regras%20de%20Negocio/Reserva%20de%20Estoque.md))
e o orçamento depende de um snapshot consistente das reservas ativas no
momento do cálculo. O requisito obrigatório do Tech Challenge pede um "Banco
de Dados Gerenciado (PostgreSQL, MySQL, SQL Server, etc.)", com liberdade de
escolha.

## Alternativas Consideradas

| Alternativa | Prós | Contras |
|---|---|---|
| **PostgreSQL (Amazon RDS)** | ACID completo, suporte nativo a `JSONB` (usado em `service.service_status` e `service_order.list_service`), extensões (`uuid-ossp`, `pgcrypto`), free tier `db.t3.micro` no AWS Academy, já usado nas fases anteriores do projeto | Exige gestão de conexões (pool) e migração de schema manual (sem Flyway/Liquibase neste projeto) |
| **MySQL (Amazon RDS)** | Também gratuito no free tier, amplamente usado no mercado | Suporte a JSON mais limitado que o Postgres; time sem histórico de uso no projeto |
| **SQL Server (Amazon RDS)** | Boa integração com ferramentas Microsoft | Licenciamento mais restritivo mesmo no free tier; sem vantagem concreta para este domínio |
| **DynamoDB (NoSQL gerenciado)** | Serverless, escala automaticamente | Modelo de domínio é fortemente relacional (FKs entre `clients`, `vehicles`, `service_order`, `stocks`, `budgets`); modelar joins e transações multi-item em DynamoDB aumentaria a complexidade sem necessidade real de escala do projeto |

## Decisão

Manter o **PostgreSQL 16**, provisionado como **Amazon RDS** (`db.t3.micro`)
pelo repositório `os-management-database`.

Motivos centrais:

1. **Aderência ao modelo relacional do domínio** — `service_order`,
   `stock_reservations`, `budgets` e `budget_items` têm relacionamentos e
   regras de consistência (ex.: uma reserva só existe se referenciar um
   `stock` e uma `service_order` válidos) que se beneficiam de FKs e
   transações ACID nativas.
2. **JSONB para dados semiestruturados** — os campos `service.service_status`
   e `service_order.list_service` guardam listas/estados que variam em forma
   sem precisar de tabelas adicionais, aproveitando o suporte nativo do
   Postgres a `JSONB` sem abrir mão do restante do modelo relacional.
3. **Continuidade** — o time já usava PostgreSQL nas fases anteriores do
   Tech Challenge; migrar de banco não traria benefício técnico proporcional
   ao custo de retrabalho (novos scripts DDL, mapeamento JPA, testes de
   integração com Testcontainers já escritos para Postgres).
4. **Custo** — coberto pelo free tier do AWS Academy Learner Lab, alinhado à
   escolha de provedor de nuvem ([RFC-0001](RFC-0001-escolha-provedor-nuvem.md)).

## Ajustes no Modelo Relacional

A justificativa formal da modelagem, os diagramas ER e a explicação de cada
relacionamento estão documentados em
[`os-management-database/README.md`](../../../os-management-database/README.md#modelagem-do-banco-de-dados),
que é o repositório dono do schema (`scripts/ddl.sql`).

## Consequências

- O repositório `os-management-database` passa a ser a única fonte de verdade
  do schema (DDL/DML); a aplicação (`os-management`) e a função serverless de
  autenticação (`os-management-lambda`) apenas consomem a conexão via
  `DB_URL_<ENV>`, sem gerenciar o schema.
- Não há ferramenta de migração versionada (Flyway/Liquibase) — os scripts
  `ddl.sql`/`dml.sql` são aplicados manualmente/via job de carga. Isso é aceito
  no escopo acadêmico, mas é um ponto de atenção para evolução futura do
  schema em produção real (ver seção "Trabalhos Futuros" no README do banco).
- Sequences com incremento `50` (`clients_seq`, `vehicles_seq`, etc.) seguem a
  convenção do Hibernate `allocationSize=50`, evitando round-trips ao banco a
  cada `INSERT` — uma decisão de performance herdada do ORM.

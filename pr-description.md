# CRUD de Clientes e Veículos

## Context

### O que e Por quê?

Implementação do CRUD completo para Clientes (`Client`) e Veículos (`Vehicle`), seguindo arquitetura Vertical Slice com DDD no domínio, MapStruct para mapeamento e documentação via JavaDoc.

Essas são as funcionalidades base para identificar o proprietário e o veículo alvo de cada Ordem de Serviço.

Sem esses CRUDs, não é possível vincular uma OS a um cliente ou a um veículo específico, bloqueando o fluxo principal da oficina que depende de proprietário e veículo existentes e válidos no sistema.

---

## Descrição

### Implementação

- Adição do **MapStruct 1.6.3** ao `pom.xml` com `lombok-mapstruct-binding` para garantir a ordem correta dos annotation processors na compilação

- Criação da feature `client` no pacote `com.os.features.client` com endpoints REST em `/api/clients`:
  - `POST`, `GET` (por ID, por CPF, listar ativos), `PUT` e `DELETE` (soft delete)

- Criação da feature `vehicle` no pacote `com.os.features.vehicle` com endpoints REST em `/api/vehicles`:
  - `POST`, `GET` (por ID, por placa, listar veículos do cliente), `PUT` e `DELETE` (soft delete)

- **Domínio DDD puro** — sem anotações de framework:
  - Value objects `Cpf` (valida algoritmo da Receita Federal) e `LicensePlate` (suporta formato antigo `ABC1234` e Mercosul `ABC1D23`)
  - Aggregates `Client` e `Vehicle` com factory methods `create()` e `reconstitute()` separando criação de reconstituição via persistência
  - `Vehicle` referencia o cliente por `clientId` (UUID) sem acoplar os aggregates

- **Mappers MapStruct** em pacote dedicado `persistence/mapper`, sem lógica de negócio:
  - `ClientMapper`: `toEntity`, `updateEntity` (gerados pelo MapStruct) e `toDomain` (manual via `Client.reconstitute`)
  - `VehicleMapper`: `toEntity(Vehicle, ClientEntity)` com dois parâmetros para resolver o `@ManyToOne`; `updateEntity` e `toDomain` seguindo o mesmo padrão

- Criação de records de request/response com validações via Bean Validation e factory method `from()` para conversão do domínio para o contrato da API

- **Regras de negócio implementadas nos services:**
  - Validação de CPF único na criação (o CPF não pode ser alterado após cadastro)
  - Validação de placa única na criação e atualização
  - Não é possível cadastrar um veículo sem informar um cliente válido — `VehicleService` valida a existência do cliente via `ClientRepository` antes de persistir
  - Busca por ID, CPF (cliente) e placa (veículo) com erro 404 descritivo caso não encontrado
  - Listagem apenas de registros ativos
  - Desativação via soft delete (registro permanece no banco com `active = false`)

- **Atualização do `GlobalExceptionHandler`** com handler dedicado para `ClientNotFoundException` e `VehicleNotFoundException` → HTTP 404

---

### Teste

#### Testes Unitários

| Id | Cenário | Input | Output | Notas |
|:--:|---------|-------|--------|-------|
| 1  | Criar cliente com CPF único | `ClientRequest` válido | `Client` salvo e retornado | Valida chamada ao repositório |
| 2  | Criar cliente com CPF duplicado | CPF já existente | `IllegalStateException` | — |
| 3  | Criar cliente com CPF inválido | CPF com dígito verificador errado | `IllegalArgumentException` | Lançado pelo value object `Cpf` |
| 4  | Buscar cliente por ID existente | UUID válido | `Client` correspondente | — |
| 5  | Buscar cliente por ID inexistente | UUID inválido | `ClientNotFoundException` | HTTP 404 |
| 6  | Buscar cliente por CPF existente | CPF válido | `Client` correspondente | Aceita entrada com e sem formatação |
| 7  | Buscar cliente por CPF inexistente | CPF não cadastrado | `ClientNotFoundException` | HTTP 404 |
| 8  | Listar clientes ativos | — | Lista filtrada por `active = true` | — |
| 9  | Desativar cliente | UUID válido | `Client` com `active = false` | Soft delete |
| 10 | Criar veículo com cliente válido e placa única | `VehicleRequest` válido | `Vehicle` salvo e retornado | — |
| 11 | Criar veículo com cliente inexistente | `clientId` inválido | `ClientNotFoundException` | HTTP 404 |
| 12 | Criar veículo com placa duplicada | Placa já existente | `IllegalStateException` | — |
| 13 | Criar veículo com placa inválida | Formato fora do padrão brasileiro | `IllegalArgumentException` | Lançado pelo value object `LicensePlate` |
| 14 | Buscar veículo por ID existente | UUID válido | `Vehicle` correspondente | — |
| 15 | Buscar veículo por ID inexistente | UUID inválido | `VehicleNotFoundException` | HTTP 404 |
| 16 | Buscar veículo por placa existente | Placa válida | `Vehicle` correspondente | Aceita entrada com e sem hífen |
| 17 | Buscar veículo por placa inexistente | Placa não cadastrada | `VehicleNotFoundException` | HTTP 404 |
| 18 | Listar veículos de cliente existente | `clientId` válido | Lista de veículos ativos do cliente | — |
| 19 | Listar veículos de cliente inexistente | `clientId` inválido | `ClientNotFoundException` | HTTP 404 |
| 20 | Atualizar veículo com nova placa única | `VehicleRequest` com placa diferente | `Vehicle` atualizado | Valida unicidade da nova placa |
| 21 | Atualizar veículo com placa de outro veículo | Placa já pertencente a outro registro | `IllegalStateException` | — |
| 22 | Desativar veículo | UUID válido | `Vehicle` com `active = false` | Soft delete |

#### Testes de Integração

Testes E2E com MockMvc (standalone setup) serão incluídos na PR de testes, cobrindo o fluxo HTTP: `Controller → Service → Repository mockado`.  
Não há chamadas a sistemas externos nestes commits.

---

### Critérios de Aceite

- Endpoints `/api/clients` e `/api/vehicles` devem responder: `201` (criação), `200` (consulta/atualização), `204` (desativação), `400` (entrada inválida), `404` (recurso não encontrado), `409` (duplicidade)
- CPF deve ser único por cliente — tentativa de cadastro com CPF existente retorna `409` com mensagem descritiva
- Placa deve ser única no sistema — tentativa de cadastro com placa existente retorna `409` com mensagem descritiva
- Não deve ser possível criar um veículo sem informar um `clientId` válido — retorna `404` com mensagem descritiva
- CPF deve passar pela validação dos dígitos verificadores (algoritmo da Receita Federal) — CPF inválido retorna `400`
- Placa deve estar no formato antigo (`ABC1234`) ou Mercosul (`ABC1D23`) — formato inválido retorna `400`

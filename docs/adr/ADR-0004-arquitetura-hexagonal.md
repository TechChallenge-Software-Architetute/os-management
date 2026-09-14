# ADR-0004: Arquitetura Hexagonal (Ports & Adapters) no Domínio

- **Status:** Aceito
- **Data:** 2026-09-13

## Contexto

O backend (`os-management`) concentra regras de negócio ricas e com
invariantes claras: máquina de estados da OS, reserva atômica de estoque,
snapshot de preço no orçamento, soft delete. Era preciso escolher uma
organização de camadas que (a) isolasse essas regras de detalhes de
infraestrutura (JPA, Spring Web, SNS) e (b) permitisse trocar adapters
(ex.: provider de notificação, banco) sem reescrever regras de negócio.

## Decisão

Adotar **Arquitetura Hexagonal (Ports & Adapters)**, com quatro camadas:

```
adapter/in/web        → Controllers REST (entrada HTTP)
application           → Use Cases + Port interfaces (saída)
domain                 → Entidades ricas, Value Objects, eventos — sem
                          dependência de frameworks externos
adapter/out/persistence → Implementações de Port (JPA, mappers MapStruct)
infrastructure          → Config, Security, SNS, exception handling
```

O domínio define **interfaces** (ex.: `ClientRepository`,
`EmailNotificationPort`); a infraestrutura fornece as **implementações**
(`ClientJpaRepository`, `SnsNotificationService`). O domínio nunca importa
`javax.persistence` nem classes do Spring.

## Consequências

- **Testabilidade:** regras de negócio (`ServiceOrder`, `Stock`, `Budget`) são
  testadas sem subir contexto Spring nem banco — apenas objetos Java puros.
  Contribuiu para a cobertura mínima de 90% (JaCoCo) exigida no projeto.
- **Trocar o provider de notificação é uma mudança isolada:** o `EmailNotificationPort`
  permitiu adotar SNS em vez de SES ([RFC-0001](../rfc/RFC-0001-escolha-provedor-nuvem.md))
  sem alterar nenhuma regra de negócio — apenas a implementação do adapter
  (`SnsNotificationService`).
- **Curva de entrada maior para novos desenvolvedores:** a separação em
  4 camadas por módulo de domínio exige entender o fluxo
  Controller → UseCase → Domínio → Adapter antes de contribuir; mitigado
  pela documentação em `os-tech-documentation/01 - Arquitetura/Padroes de Projeto.md`.
- **Mapeamento explícito entre camadas** (DTO ↔ Domínio ↔ Entidade JPA via
  MapStruct) adiciona classes de mapeamento, mas evita vazar detalhes de
  persistência (anotações JPA, lazy loading) para o domínio.

## Alternativas Rejeitadas

- **Arquitetura em camadas tradicional (Controller → Service → Repository,
  sem interfaces de domínio):** rejeitada por acoplar regra de negócio a
  `@Entity`/JPA diretamente, dificultando testes unitários puros e a troca de
  provider de notificação sem tocar em código de domínio.
- **Arquitetura orientada a microsserviços por módulo de domínio** (Client,
  Stock, Budget como serviços HTTP separados): avaliada, mas rejeitada por
  introduzir complexidade de comunicação distribuída (rede, consistência
  eventual) desproporcional ao volume de dados/tráfego do desafio — o
  desacoplamento necessário já é obtido com Hexagonal + eventos in-process
  (ver [ADR-0001](ADR-0001-padrao-comunicacao.md)).

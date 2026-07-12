# Roteiro — Vídeo Demonstrativo OS Management (Fase 2)
# Duração alvo: ~13-14 minutos

---

## PREPARAÇÃO ANTES DE GRAVAR

### O que deve estar aberto e pronto

- [ ] Navegador com as abas abertas:
  - GitHub Actions do repositório (na aba do último pipeline rodando ou já rodado)
  - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
  - AWS Console → EC2 → Instâncias (região us-east-1)
- [ ] Bruno aberto com a collection `bruno/os-management-api/`
- [ ] Terminal com `kubectl get all -n os-management` pronto para rodar
- [ ] VS Code ou similar com a estrutura do projeto aberta
- [ ] Docker Desktop rodando com Kubernetes habilitado

### O que deve estar rodando antes de gravar

```bash
# Subir a aplicação local (para demo das APIs)
docker compose up -d

# Verificar que está OK
curl http://localhost:8080/v3/api-docs | head -5
```

### Dados que devem estar cadastrados antes da demo de APIs

Execute o script de validação para popular o banco:
```bash
./validation.sh
```
Ou cadastre manualmente via Bruno na sequência: login → cliente → veículo → peça → estoque → OS.

---

## SEGMENTO 1 — Apresentação e Contexto (0:00 – 0:45)

### O que mostrar na tela
- Slide simples ou README.md aberto no navegador, seção "OS Management"

### Fala
> "Olá! Neste vídeo vamos demonstrar a entrega da Fase 2 do projeto OS Management,
> desenvolvido como parte da pós-graduação da FIAP.
>
> O sistema é uma API REST completa para gestão de ordens de serviço de uma oficina mecânica.
> Nesta fase, evoluímos a aplicação com arquitetura hexagonal, testes automatizados,
> containerização com Docker, orquestração com Kubernetes, infraestrutura como código
> com Terraform, e um pipeline completo de CI/CD com GitHub Actions.
>
> O vídeo segue esta ordem: arquitetura, pipeline CI/CD, infraestrutura Terraform,
> consumo das APIs e escalabilidade automática."

---

## SEGMENTO 2 — Arquitetura da Solução (0:45 – 2:30)

### O que mostrar na tela
- README aberto na seção "Visao Geral da Arquitetura da Aplicacao" (diagrama hexagonal)
- Depois mudar para seção "Arquitetura da Infraestrutura" (diagrama ASCII com K8s e AWS)

### Fala — Arquitetura da aplicação
> "A aplicação adota Arquitetura Hexagonal — Ports & Adapters.
>
> No centro temos o **domínio puro**, sem dependências de frameworks:
> a entidade ServiceOrder implementa a máquina de estados da OS com transições validadas.
> Uma tentativa de transição inválida retorna HTTP 422 diretamente do domínio.
>
> Em volta, a camada de **aplicação** com os Use Cases:
> CreateOrder, UpdateOrder, DecideOrder, ReserveStock — cada um responsável por um único fluxo.
>
> Nas bordas, os **adaptadores**: os Controllers REST que recebem as requisições,
> e os Persistence Adapters que implementam os repositórios usando JPA.
>
> A notificação por email via AWS SNS é uma porta de saída — a interface EmailNotificationPort
> isola o domínio do provider externo. A chamada é assíncrona com @Async,
> então uma falha no SNS não impacta a operação de negócio."

### Fala — Arquitetura da infraestrutura
> "Na infraestrutura, temos dois modos de operação:
>
> **Local**: a aplicação roda em Kubernetes no Docker Desktop.
> O Terraform provisiona o namespace, o banco PostgreSQL em cluster,
> ConfigMaps, Secrets, o Deployment da aplicação, o HPA e o Ingress.
>
> **AWS**: EC2 t3.micro rodando Docker com a imagem publicada no Docker Hub,
> e RDS db.t3.micro com PostgreSQL 16 — ambos dentro do free tier.
> A conexão entre EC2 e RDS é isolada por Security Groups."

---

## SEGMENTO 3 — Pipeline CI/CD (2:30 – 5:30)

### O que mostrar na tela
1. Repositório GitHub → aba **Actions** → último pipeline executado
2. Clicar em cada job para mostrar os steps

### Fala — Visão geral do pipeline
> "Vamos ao pipeline. Qualquer push para o repositório dispara automaticamente
> todos os jobs em sequência."

### Mostrar job `unit-test` (clicar nele)
> "O primeiro job executa todos os testes unitários com Maven.
> O JaCoCo verifica a cobertura mínima de 90% — o build falha se não atingir.
> O relatório de cobertura é publicado como artefato para consulta."

### Mostrar job `build`
> "Depois do teste, o build empacota o JAR com `mvn package`."

### Mostrar job `publish`
> "O job de publish faz o bump automático da versão no `pom.xml`,
> publica o pacote no GitHub Packages, e emite a versão como output
> para os jobs seguintes usarem."

### Mostrar job `docker-build-push`
> "Aqui está o ponto crítico: o build da imagem Docker.
> Usamos `docker buildx` com `--platform linux/amd64` —
> isso garante compatibilidade com a EC2 na AWS, que é x86.
> A imagem é publicada no Docker Hub com três tags:
> latest, versão completa, e versão com hash do commit."

### Mostrar job `terraform-ec2`
> "Por fim, o job de deploy.
> Ele configura as credenciais AWS, inicializa o Terraform com o backend S3
> — onde o state file fica persistido entre execuções —
> e aplica o `terraform/ec2/` com as variáveis vindas dos secrets do GitHub.
>
> A imagem passada ao Terraform usa a tag versionada gerada no job anterior.
> Quando a tag muda, o Terraform recria a EC2 com o novo `user_data`,
> puxando automaticamente a nova imagem do Docker Hub."

### Mostrar o output da URL
> "No final do job, o Terraform exibe a URL pública da aplicação,
> gerada a partir do IP público da EC2."

---

## SEGMENTO 4 — Infraestrutura com Terraform (5:30 – 7:30)

### O que mostrar na tela
1. VS Code aberto em `terraform/ec2/main.tf`
2. Depois `k8s/hpa.yaml`
3. Depois terminal: `kubectl get all -n os-management`

### Fala — Terraform EC2
> "Vamos ver o código Terraform que provisiona a infraestrutura AWS.
>
> [Mostrar main.tf]
>
> Temos dois Security Groups: um para a EC2 liberando as portas 8080 e 22,
> e outro para o RDS que só aceita conexão na porta 5432 vinda do SG da EC2.
> Isso garante que o banco nunca fica exposto à internet.
>
> O RDS é `db.t3.micro` com PostgreSQL 16 e `skip_final_snapshot = true`
> para facilitar destruição nos testes.
>
> A EC2 usa `user_data` — um script shell que roda na inicialização,
> instala o Docker e executa o container com todas as variáveis de ambiente
> apontando para o endpoint do RDS."

### Fala — HPA
> "Para escalabilidade, aqui está o HPA — HorizontalPodAutoscaler.
>
> [Mostrar hpa.yaml]
>
> Configurado com mínimo de 2 réplicas e máximo de 6.
> Escala quando CPU ultrapassa 70% ou memória ultrapassa 75%.
> O comportamento de scale-up é agressivo: dobra os pods em 60 segundos.
> O scale-down é conservador: aguarda 5 minutos antes de reduzir."

### Fala — Kubernetes local
> "Rodando local com Docker Desktop:
>
> [Rodar no terminal: kubectl get all -n os-management]
>
> Vemos o namespace, os 2 pods rodando, os services e o HPA monitorando."

---

## SEGMENTO 5 — Consumo das APIs (7:30 – 11:30)

### O que mostrar na tela
- Bruno ou Swagger UI

### Parte A: Autenticação (7:30 – 8:00)

> "Todas as APIs exigem autenticação via JWT.
>
> [POST /auth/login]
>
> Fazemos o login com o usuário administrador padrão.
> O token retornado é automaticamente usado nas próximas requisições pela collection Bruno."

```json
POST /auth/login
{ "email": "superadmin@system.com", "password": "coxinha123" }
```

### Parte B: Abertura da OS — Requisito obrigatório (8:00 – 9:00)

> "O primeiro requisito obrigatório é a **abertura da Ordem de Serviço**.
> O endpoint recebe cliente, veículo e serviços, e retorna a identificação única da OS.
>
> [Mostrar POST /order]
>
> A OS é criada no status RECEBIDA. Já temos o ID único que usaremos nos próximos steps."

### Parte C: Consulta de status — Requisito obrigatório (9:00 – 9:30)

> "**Consulta de status da OS** — mostra a situação atual.
>
> [GET /order/id/{id}]
>
> Status atual: RECEBIDA. A resposta inclui todos os dados da OS,
> os serviços solicitados e o histórico de transições."

### Parte D: Fluxo completo da OS (9:30 – 10:30)

> "Vamos avançar o ciclo de vida da OS, mostrando as transições de status:
>
> [PATCH /order/{id} → EM_DIAGNOSTICO]
> O mecânico inicia o diagnóstico.
>
> [POST /api/stocks/reservations — reservar peças]
> Durante o diagnóstico, reservamos peças. A reserva é atômica:
> ou reserva tudo ou não reserva nada.
>
> [GET /api/budgets/service-order/{id}]
> O orçamento é gerado automaticamente com base nas reservas.
> O preço é um snapshot — congelado no momento do cálculo.
>
> [PATCH /order/{id} → AGUARDANDO_APROVACAO]
> OS vai para aguardar aprovação do cliente.
>
> [POST /api/clients/my-orders/{id}/decision — APPROVED]"

### Parte E: Aprovação do orçamento — Requisito obrigatório (10:30 – 11:00)

> "**Aprovação de orçamento** — endpoint que recebe a decisão do cliente.
>
> [Mostrar o payload com decision: APPROVED]
>
> Ao aprovar, a OS vai para APROVADO. Ao recusar, as reservas de estoque
> são liberadas automaticamente via Spring Event.
>
> Vamos aprovar e continuar o fluxo:
>
> [PATCH → EM_EXECUCAO → FINALIZADA → ENTREGUE]
>
> Pronto — ciclo completo de uma OS do início à entrega."

### Parte F: Listagem ordenada — Requisito obrigatório (11:00 – 11:30)

> "**Listagem de ordens de serviço** com ordenação por status.
>
> [GET /order]
>
> A resposta já vem ordenada: EM_EXECUCAO primeiro, depois AGUARDANDO_APROVACAO,
> EM_DIAGNOSTICO e RECEBIDA. OS finalizadas e entregues são excluídas logicamente
> — não aparecem na listagem padrão, mas ficam no histórico.
>
> Para consultar o histórico completo por CPF ou CNPJ:
>
> [GET /order/document/{cpfCnpj}]
>
> Todas as OS do cliente aparecem, incluindo as entregues."

---

## SEGMENTO 6 — Escalabilidade Automática (11:30 – 13:30)

### O que mostrar na tela
1. Terminal com `kubectl get hpa -n os-management -w` (watch)
2. Segundo terminal gerando carga

### Fala — Antes da carga
> "Para demonstrar a escalabilidade automática, vamos simular aumento de carga
> e observar o HPA ajustar o número de réplicas.
>
> [Terminal 1:]
> ```bash
> kubectl get hpa os-management -n os-management -w
> ```
>
> Vemos: 2 réplicas rodando, CPU atual em X% dos 70% configurados como threshold."

### Gerar carga
```bash
# Terminal 2 — gerar requisições simultâneas
for i in $(seq 1 200); do
  curl -s http://localhost:8080/order \
    -H "Authorization: Bearer SEU_TOKEN" > /dev/null &
done
wait
```

> "Disparamos 200 requisições simultâneas. Observe o HPA reagir:
> a CPU sobe acima de 70%, e em até 60 segundos o HPA escala de 2 para 4 pods.
>
> [Aguardar e mostrar a mudança no watch]
>
> Agora temos [X] réplicas ativas. O Kubernetes distribuiu automaticamente
> a carga entre elas sem intervenção manual.
>
> Ao remover a carga, o HPA aguarda o período de estabilização de 5 minutos
> antes de reduzir — evitando oscilações desnecessárias."

### Alternativa para mostrar HPA sem gerar carga real
> Se o HPA não escalar durante a gravação, pode mostrar o status atual e explicar:
```bash
kubectl describe hpa os-management -n os-management
```
> "Aqui vemos o HPA configurado: min 2, max 6 réplicas,
> monitorando CPU a 70% e memória a 75%.
> Em produção com múltiplas OS sendo processadas simultaneamente,
> o HPA escalaria automaticamente para atender a demanda."

---

## SEGMENTO 7 — Deploy na AWS e Encerramento (13:30 – 14:30)

### O que mostrar na tela
1. AWS Console → EC2 → Instância rodando
2. Swagger UI no IP público da EC2

### Fala
> "Para fechar, vamos ver a aplicação rodando na AWS.
>
> [AWS Console — mostrar instância EC2 Running]
>
> Aqui está a instância EC2 t3.micro no free tier — criada pelo Terraform
> via pipeline. E aqui o RDS PostgreSQL db.t3.micro também no free tier.
>
> [Abrir Swagger no IP público]
>
> A aplicação está acessível publicamente.
> O mesmo Swagger que rodamos local está aqui na AWS,
> conectado ao banco RDS — toda a infra provisionada por código."

### Encerramento
> "Resumindo o que entregamos nesta fase:
>
> - Aplicação refatorada com Arquitetura Hexagonal e Clean Code
> - Testes automatizados com cobertura mínima de 90%
> - Containerização com Docker, docker-compose para desenvolvimento local
> - Manifestos Kubernetes: Deployment, Service, ConfigMap, Secret e HPA
> - Infraestrutura como Código com Terraform: cluster local e EC2 + RDS na AWS
> - Pipeline CI/CD completo: testes, build, publicação da imagem, deploy automático
>
> O código está no repositório GitHub e a collection das APIs está na pasta /bruno.
> Obrigado!"

---

## CHECKLIST FINAL — Requisitos cobertos no vídeo

| Requisito | Segmento | Coberto |
|-----------|----------|---------|
| Arquitetura Hexagonal | Seg. 2 | ✅ |
| Clean Code | Seg. 2 (explicado) | ✅ |
| Testes automatizados | Seg. 3 (pipeline unit-test) | ✅ |
| Abertura de OS | Seg. 5B | ✅ |
| Consulta de status da OS | Seg. 5C | ✅ |
| Aprovação de orçamento | Seg. 5E | ✅ |
| Listagem ordenada por status | Seg. 5F | ✅ |
| Exclusão lógica de finalizadas/entregues | Seg. 5F | ✅ |
| Docker / docker-compose | Seg. 4 (mencionado) | ✅ |
| Kubernetes Deployments | Seg. 4 + kubectl | ✅ |
| Kubernetes Services | Seg. 4 + kubectl | ✅ |
| ConfigMaps e Secrets | Seg. 4 (Terraform) | ✅ |
| HPA | Seg. 4 + Seg. 6 | ✅ |
| Terraform K8s local | Seg. 4 | ✅ |
| Terraform banco de dados | Seg. 4 (RDS) | ✅ |
| CI/CD — Build | Seg. 3 | ✅ |
| CI/CD — Testes | Seg. 3 | ✅ |
| CI/CD — Build imagem Docker | Seg. 3 | ✅ |
| CI/CD — Deploy Kubernetes | Seg. 3 | ✅ |
| CI/CD — Deploy banco | Seg. 3 (RDS via Terraform) | ✅ |
| CI/CD — Apply manifestos YAML | Seg. 3 | ✅ |
| Escalabilidade automática | Seg. 6 | ✅ |
| Consumo das APIs | Seg. 5 | ✅ |

---

## DICAS DE GRAVAÇÃO

- **Resolução**: 1920x1080 mínimo
- **Fonte do terminal**: aumentar para pelo menos 16pt (visibilidade)
- **Bruno vs Swagger**: Bruno é melhor para o vídeo pois mostra request e response lado a lado
- **Velocidade**: não correr nas transições de status — dar tempo para o espectador ler o response
- **HPA**: caso não consiga gerar carga real suficiente durante a gravação, use `kubectl patch` para forçar o scale e explique que em produção o HPA faz isso automaticamente baseado em métricas reais
- **Cortes**: pode editar e cortar as esperas longas (ex: terraform apply levando 8 min para o RDS)
- **Voz**: mantenha o tom calmo e técnico — não é necessário cobrir tudo nos mínimos detalhes, foque nos pontos obrigatórios

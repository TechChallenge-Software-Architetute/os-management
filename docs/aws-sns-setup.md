# Configuracao AWS SNS — Passo a Passo

Guia completo para configurar o AWS SNS no ambiente AWS Academy e integrar com a aplicacao OS Management.

---

## Passo 1 — Iniciar o Lab e pegar credenciais

1. Acesse o **AWS Academy** e clique em **Start Lab**
2. Aguarde o indicador ficar verde
3. Clique em **AWS Details** (canto superior direito)
4. Clique **Show** ao lado de **AWS CLI**
5. Voce vera algo assim:

```
[default]
aws_access_key_id=ASIAXXXXXXXXXXXXXXXXXXX
aws_secret_access_key=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
aws_session_token=xxxxxxxxxxxxxxx(token muito longo)xxxxxxxxxxxxxxx
```

6. Copie os 3 valores (access key, secret, session token)

---

## Passo 2 — Abrir o Console AWS

1. No AWS Academy, clique no botao **AWS** (verde, ao lado de Start Lab)
2. Isso abre o Console AWS no navegador
3. No canto superior direito, confirme que a regiao e **N. Virginia (us-east-1)**
   - Se nao for, clique no nome da regiao e selecione **US East (N. Virginia) us-east-1**

---

## Passo 3 — Criar o Topico SNS

1. Na barra de busca do console, digite **SNS** e clique em **Simple Notification Service**
2. No menu lateral, clique em **Topics**
3. Clique no botao laranja **Create topic**
4. Preencha:
   - **Type**: Standard
   - **Name**: `os-management-notifications`
   - Deixe todo o resto como padrao
5. Clique em **Create topic**
6. Na tela do topico criado, copie o **ARN** — fica no topo, algo como:

```
arn:aws:sns:us-east-1:837687730031:os-management-notifications
```

---

## Passo 4 — Adicionar email como Subscriber

1. Ainda na tela do topico, clique em **Create subscription**
2. Preencha:
   - **Protocol**: Email
   - **Endpoint**: seu email (ex: `amanda.lcosta33@gmail.com`)
3. Clique em **Create subscription**
4. Abra sua caixa de email (pode demorar 1-2 minutos)
5. Procure um email da **AWS Notifications** com assunto "AWS Notification - Subscription Confirmation"
6. Clique no link **Confirm subscription** dentro do email
7. Volte ao console AWS — o status da subscription deve mudar para **Confirmed**

Se nao encontrar o email, verifique a pasta Spam/Lixo Eletronico.

---

## Passo 5 — Preencher o arquivo .env

Abra o arquivo `.env` na raiz do projeto `os-management` e preencha com os dados coletados:

```env
AWS_REGION=us-east-1
AWS_SNS_TOPIC_ARN=arn:aws:sns:us-east-1:837687730031:os-management-notifications
AWS_ACCESS_KEY_ID=ASIAXXXXXXXXXXXXXXXXXXX
AWS_SECRET_ACCESS_KEY=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
AWS_SESSION_TOKEN=xxxxxxxxxxxxxxx(token muito longo)xxxxxxxxxxxxxxx
```

Substitua:
- `AWS_SNS_TOPIC_ARN` pelo ARN que voce copiou no Passo 3
- As 3 credenciais pelos valores que copiou no Passo 1

---

## Passo 6 — Subir a aplicacao

```bash
docker compose up --build
```

---

## Passo 7 — Testar

Faca login e crie uma OS (ou rode o `./validation.sh`). A cada mudanca de status, voce recebe um email:

```
Assunto: OS Management - Atualizacao de Status: Em Diagnostico

============================================
OS Management - Notificacao de Status
============================================

Ola JOAO DA SILVA,

Seu veiculo esta sendo avaliado pelo nosso mecanico.

Status atual: Em Diagnostico
Veiculo: TOYOTA COROLLA (ABC1234)

--------------------------------------------
Oficina Mecanica - OS Management
Este e um email automatico.
============================================
```

---

## Atualizacao de Credenciais (a cada sessao do Lab)

As credenciais do AWS Academy expiram a cada sessao do Lab (~4h). Quando o Lab reiniciar:

1. Inicie o Lab novamente
2. Clique em **AWS Details** > **Show**
3. Copie as novas credenciais
4. Atualize o `.env` com os novos valores (apenas as 3 ultimas linhas)
5. Reinicie a aplicacao: `docker compose down && docker compose up --build`

O ARN do topico SNS nao muda — so precisa atualizar as credenciais.

---

## Troubleshooting

| Problema | Solucao |
|----------|---------|
| Invalid parameter: TopicArn | Verifique se o ARN no `.env` esta correto e completo |
| The security token is expired | Copie novas credenciais do AWS Details (Lab expirou) |
| Email nao chega | Verifique se confirmou a subscription (Passo 4) |
| Email no Spam | Normal na primeira vez — marque como "Nao e spam" |
| Unable to load credentials | Verifique se o `.env` esta na raiz do projeto e sem espacos extras |
| Topic does not exist | Confirme que a regiao no `.env` e a mesma onde criou o topico |
| SignatureDoesNotMatch | As credenciais podem ter espacos em branco — copie novamente sem espacos |

---

## Resumo Visual

```
AWS Academy Lab
    |
    +-- AWS Details -> Credenciais (Access Key + Secret + Token)
    |
    +-- Console AWS
            |
            +-- SNS > Topics
                    |
                    +-- Create topic: "os-management-notifications" -> copiar ARN
                    |
                    +-- Create subscription: Email -> confirmar no email

Arquivo .env (raiz do projeto)
    |
    +-- AWS_REGION = us-east-1
    +-- AWS_SNS_TOPIC_ARN = ARN copiado do console
    +-- AWS_ACCESS_KEY_ID = da AWS Details
    +-- AWS_SECRET_ACCESS_KEY = da AWS Details
    +-- AWS_SESSION_TOKEN = da AWS Details

docker compose up --build -> app publica no SNS -> voce recebe email
```

---

## Observacoes Importantes

- O arquivo `.env` esta no `.gitignore` — nunca sera commitado no repositorio
- O topico SNS persiste entre sessoes do Lab (nao precisa recriar)
- A subscription tambem persiste — so precisa confirmar uma vez
- Se adicionar mais emails como subscribers, cada um recebe todas as notificacoes
- A aplicacao funciona normalmente mesmo sem SNS configurado (falha silenciosa em log)

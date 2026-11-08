# language: pt
Funcionalidade: Saga da OS do lado do os-management
  O os-management abre a saga com CRIAR_OS, executa os comandos que o
  os-orchestrator manda (EXECUTAR_SERVICO e ATUALIZAR_ESTOQUE), responde cada
  um com a mesma acao e desfaz a OS quando recebe uma compensacao.

  Contexto:
    Dado um cliente com veículo cadastrado
    E uma peça com preço de venda "89.90" e 10 unidades em estoque

  Cenário: Abertura da OS inicia a saga
    Quando a oficina abre uma OS
    Então a OS fica com status "RECEBIDA"
    E o orquestrador recebe "CRIAR_OS"

  Cenário: Orquestrador libera a execução de uma OS aprovada
    Dado uma OS aprovada pelo cliente com 2 unidades da peça reservadas
    Quando o orquestrador manda "EXECUTAR_SERVICO"
    Então a OS fica com status "EM_EXECUCAO"
    E o orquestrador recebe "EXECUTAR_SERVICO"

  Cenário: Orquestrador manda baixar o estoque da OS em execução
    Dado uma OS em execução com 2 unidades da peça reservadas
    Quando o orquestrador manda "ATUALIZAR_ESTOQUE"
    Então o estoque disponível da peça fica em 8
    E o orçamento da OS continua em "179.80"
    E o orquestrador recebe "ATUALIZAR_ESTOQUE"

  Cenário: Serviço negado pela saga cancela a OS e devolve o estoque
    Dado uma OS aprovada pelo cliente com 2 unidades da peça reservadas
    Quando o orquestrador manda "NEGAR_SERVICO"
    Então a OS fica com status "CANCELADA"
    E o estoque disponível da peça volta para 10
    E o orquestrador não recebe nada

  Cenário: Comando repetido não executa a OS de novo
    Dado uma OS em execução com 2 unidades da peça reservadas
    Quando o orquestrador manda "EXECUTAR_SERVICO"
    Então a OS continua com status "EM_EXECUCAO"
    E o orquestrador recebe "EXECUTAR_SERVICO"

  Cenário: Execução antes da aprovação é recusada
    Dado uma OS recém-aberta
    Quando o orquestrador manda "EXECUTAR_SERVICO"
    Então o comando é recusado
    E a OS continua com status "RECEBIDA"
    E o orquestrador não recebe nada

  Cenário: Cliente acompanha a OS em execução
    Dado uma OS em execução com 2 unidades da peça reservadas
    Quando o cliente consulta suas ordens de serviço
    Então ele vê a OS com status "EM_EXECUCAO" e orçamento de "179.80"

  Cenário: Orçamento recusado pelo cliente devolve o estoque
    Dado uma OS aguardando aprovação com 2 unidades da peça reservadas
    Quando o cliente recusa o orçamento
    Então a OS fica com status "RECUSADA"
    E o estoque disponível da peça volta para 10
    E o orquestrador não recebe nada

  Cenário: Transição inválida não avança a OS
    Dado uma OS recém-aberta
    Quando a oficina tenta mover a OS para "EM_EXECUCAO"
    Então a operação é recusada com status HTTP 422
    E a OS continua com status "RECEBIDA"
    E o orquestrador não recebe nada

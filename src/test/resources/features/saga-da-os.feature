# language: pt
Funcionalidade: Saga da OS do lado do os-management
  O os-management abre a saga com CRIAR_OS, executa os comandos que o
  os-orchestrator manda (EXECUTAR_SERVICO e ATUALIZAR_ESTOQUE), responde cada
  um com a mesma acao e desfaz a OS quando recebe uma compensacao.

  Contexto:
    Dado que existe o tipo de serviço "TROCA_OLEO"
    E que existe um cliente com veículo cadastrado
    E que existe uma peça com preço de venda "89.90" e 10 unidades em estoque

  Cenário: Fluxo completo da abertura até a baixa do estoque
    Quando a oficina abre uma OS com o serviço "TROCA_OLEO"
    Então a OS fica com status "RECEBIDA"
    E o orquestrador recebeu "CRIAR_OS" nessa ordem
    Quando a oficina move a OS para "EM_DIAGNOSTICO"
    E a oficina reserva 2 unidades da peça para a OS
    E a oficina move a OS para "AGUARDANDO_APROVACAO"
    Então o orçamento da OS soma "179.80"
    Quando o cliente aprova o orçamento
    E o orquestrador manda "EXECUTAR_SERVICO" para a OS
    Então a OS fica com status "EM_EXECUCAO"
    Quando o orquestrador manda "ATUALIZAR_ESTOQUE" para a OS
    Então as reservas de estoque da OS ficam "CONFIRMED"
    E o estoque disponível da peça fica em 8
    E o orçamento da OS soma "179.80"
    E o orquestrador recebeu "CRIAR_OS, EXECUTAR_SERVICO, ATUALIZAR_ESTOQUE" nessa ordem

  Cenário: Serviço negado pela saga cancela a OS e devolve o estoque
    Quando a oficina abre uma OS com o serviço "TROCA_OLEO"
    E a oficina move a OS para "EM_DIAGNOSTICO"
    E a oficina reserva 2 unidades da peça para a OS
    E a oficina move a OS para "AGUARDANDO_APROVACAO"
    E o cliente aprova o orçamento
    E o orquestrador manda "NEGAR_SERVICO" para a OS
    Então a OS fica com status "CANCELADA"
    E as reservas de estoque da OS ficam "RELEASED"
    E o estoque disponível da peça fica em 10
    E o orçamento da OS soma "0"
    E o orquestrador recebeu "CRIAR_OS" nessa ordem

  Cenário: Comando de execução repetido não executa de novo
    Quando a oficina abre uma OS com o serviço "TROCA_OLEO"
    E a oficina move a OS para "EM_DIAGNOSTICO"
    E a oficina move a OS para "AGUARDANDO_APROVACAO"
    E o cliente aprova o orçamento
    E o orquestrador manda "EXECUTAR_SERVICO" para a OS
    E o orquestrador manda "EXECUTAR_SERVICO" para a OS
    Então a OS fica com status "EM_EXECUCAO"
    E o orquestrador recebeu "CRIAR_OS, EXECUTAR_SERVICO, EXECUTAR_SERVICO" nessa ordem

  Cenário: Execução antes da aprovação é recusada
    Quando a oficina abre uma OS com o serviço "TROCA_OLEO"
    E o orquestrador manda "EXECUTAR_SERVICO" para a OS, que é recusado
    Então a OS fica com status "RECEBIDA"
    E o orquestrador recebeu "CRIAR_OS" nessa ordem

  Cenário: Orçamento recusado pelo cliente devolve o estoque
    Quando a oficina abre uma OS com o serviço "TROCA_OLEO"
    E a oficina move a OS para "EM_DIAGNOSTICO"
    E a oficina reserva 2 unidades da peça para a OS
    E a oficina move a OS para "AGUARDANDO_APROVACAO"
    E o cliente recusa o orçamento com o motivo "Muito caro"
    Então a OS fica com status "RECUSADA"
    E as reservas de estoque da OS ficam "RELEASED"
    E o estoque disponível da peça fica em 10
    E o orçamento da OS soma "0"
    E o orquestrador recebeu "CRIAR_OS" nessa ordem

  Cenário: Transição inválida não avança a OS nem a saga
    Quando a oficina abre uma OS com o serviço "TROCA_OLEO"
    E a oficina tenta mover a OS para "EM_EXECUCAO"
    Então a operação é recusada com status HTTP 422
    E a OS fica com status "RECEBIDA"
    E o orquestrador recebeu "CRIAR_OS" nessa ordem

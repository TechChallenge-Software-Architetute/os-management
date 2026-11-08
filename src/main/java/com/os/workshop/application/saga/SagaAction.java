package com.os.workshop.application.saga;

/**
 * Acoes da saga, com os mesmos nomes do {@code StateMachineActions} do
 * os-orchestrator.
 *
 * <p>O nome viaja no atributo de mensagem {@code action} do SQS, nos dois
 * sentidos, e o corpo da mensagem e sempre o id da OS. Os nomes precisam casar
 * exatamente com os do orquestrador: e o unico ponto de acoplamento do contrato.
 *
 * <p>O enum espelha todas as acoes do orquestrador, mesmo as que nao passam pelo
 * os-management, para que uma mensagem com acao conhecida mas fora do lugar seja
 * recusada com uma mensagem clara em vez de falhar na conversao.
 */
public enum SagaAction {

    /** os-management -> orquestrador: OS aberta, inicia a saga. */
    CRIAR_OS,

    /** Orquestrador <-> os-payment. Nao e destinada ao os-management. */
    GERAR_ORCAMENTO,

    /** Orquestrador <-> os-payment. Nao e destinada ao os-management. */
    GERAR_PAGAMENTO,

    /** Orquestrador -> os-management: executar o servico. Respondida com a mesma acao. */
    EXECUTAR_SERVICO,

    /** Orquestrador -> os-management: baixar o estoque reservado. Respondida com a mesma acao. */
    ATUALIZAR_ESTOQUE,

    /** Orquestrador -> os-management: compensacao, servico negado. Sem resposta. */
    NEGAR_SERVICO,

    /** Orquestrador -> os-management: compensacao, estoque negado. Sem resposta. */
    NEGAR_ESTOQUE,

    /** Orquestrador -> os-management: a saga falhou. Tratada como compensacao. Sem resposta. */
    ERRO
}

package com.os.workshop.application.saga;

/**
 * Comandos que o os-management envia para a máquina de estados do os-orchestrator.
 *
 * <p>O nome de cada constante vai no atributo de mensagem {@code action} do SQS e
 * é desserializado do outro lado como {@code StateMachineActions}, então estes
 * nomes precisam casar exatamente com os do orquestrador.
 *
 * <p><strong>Ao adicionar uma ação nova:</strong> declare a constante aqui com o
 * mesmo nome usado no orquestrador e acrescente uma linha em
 * {@link SagaActionResolver}. Nada mais precisa mudar — os três use cases já
 * publicam o evento e o resolver decide se há comando a enviar.
 *
 * <p>O orquestrador ainda vai ganhar ações para os estados finais
 * ({@code FINALIZADA}, {@code ENTREGUE}, {@code RECUSADA}); elas entram aqui
 * quando existirem lá.
 */
public enum SagaAction {

    /** Ordem de serviço criada. */
    CRIAR_OS,

    /** Orçamento gerado e aguardando aprovação do cliente. */
    GERAR_ORCAMENTO,

    /** Serviço liberado para execução. */
    EXECUTAR_SERVICO
}

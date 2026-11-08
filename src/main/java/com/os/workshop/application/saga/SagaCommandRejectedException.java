package com.os.workshop.application.saga;

import lombok.Getter;

import java.util.UUID;

/**
 * Comando da saga que nao pode ser aplicado no estado atual da OS.
 *
 * <p>O listener deixa a excecao subir: a mensagem nao e confirmada, volta para a
 * fila e, depois do {@code maxReceiveCount}, vai para a DLQ, onde fica
 * inspecionavel. E o caminho certo para comando fora de ordem ou fora de lugar.
 */
@Getter
public class SagaCommandRejectedException extends RuntimeException {

    private final UUID orderId;
    private final SagaAction action;

    public SagaCommandRejectedException(UUID orderId, SagaAction action, String reason) {
        super("Comando " + action + " recusado para a OS " + orderId + ": " + reason);
        this.orderId = orderId;
        this.action = action;
    }
}

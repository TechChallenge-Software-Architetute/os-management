package com.os.workshop.application.saga;

import java.util.UUID;

/**
 * Comando a ser entregue ao orquestrador: qual ordem, qual passo.
 *
 * <p>O corpo da mensagem é o id da ordem e a ação viaja no atributo
 * {@code action} — o formato que o {@code StateMachineListener} do
 * os-orchestrator já espera.
 */
public record OrderSagaCommand(UUID orderId, SagaAction action) {

    public OrderSagaCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId do comando da saga e obrigatorio.");
        }
        if (action == null) {
            throw new IllegalArgumentException("action do comando da saga e obrigatoria.");
        }
    }
}

package com.os.workshop.application.saga;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Mensagem que o os-management envia ao os-orchestrator.
 *
 * <p>No SQS vira o corpo JSON {@code {"order", "action", "amount", "correlationId"}},
 * exatamente esses quatro campos. Contrato no ADR-0006.
 *
 * @param orderId       id da OS
 * @param action        passo da saga
 * @param amount        total do orcamento da OS no momento do envio (zero se ainda nao ha orcamento)
 * @param correlationId identificador que acompanha a OS pelos servicos da saga
 */
public record SagaMessage(UUID orderId, SagaAction action, BigDecimal amount, String correlationId) {

    public SagaMessage {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId da mensagem da saga e obrigatorio.");
        }
        if (action == null) {
            throw new IllegalArgumentException("action da mensagem da saga e obrigatoria.");
        }
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("correlationId da mensagem da saga e obrigatorio.");
        }
        // Valor monetario sempre com 2 casas; sem orcamento, zero.
        amount = (amount == null ? BigDecimal.ZERO : amount).setScale(2, RoundingMode.HALF_UP);
    }
}

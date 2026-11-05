package com.os.workshop.application.saga;

import java.util.UUID;

/**
 * Evento interno (mesmo JVM) publicado pelos use cases quando uma ordem avança.
 *
 * <p>Os use cases publicam este evento em vez de chamar o SQS diretamente: dois
 * dos três seams são {@code @Transactional}, e um envio síncrono dentro da
 * transação pode ser entregue e depois sofrer rollback — o orquestrador saberia
 * de uma mudança de estado que nunca aconteceu. O
 * {@link OrderSagaEventListener} faz o envio depois do commit.
 *
 * <p>{@code previousStatus} é nulo na criação da ordem, que é o que distingue
 * "ordem criada" de "ordem avançou".
 */
public record OrderSagaEvent(UUID orderId, String previousStatus, String newStatus) {

    public static OrderSagaEvent created(UUID orderId, String newStatus) {
        return new OrderSagaEvent(orderId, null, newStatus);
    }

    public static OrderSagaEvent advanced(UUID orderId, String previousStatus, String newStatus) {
        return new OrderSagaEvent(orderId, previousStatus, newStatus);
    }

    public boolean isCreation() {
        return previousStatus == null;
    }
}

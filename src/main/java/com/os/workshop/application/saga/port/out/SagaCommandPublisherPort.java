package com.os.workshop.application.saga.port.out;

import com.os.workshop.application.saga.SagaMessage;

/**
 * Porta de saída para entregar comandos à máquina de estados do os-orchestrator.
 *
 * <p>Mesma convenção do {@code EmailNotificationPort}: a interface fica na camada
 * de aplicação e a implementação com o SDK da AWS fica em
 * {@code infrastructure/saga}, de modo que nenhum tipo do SDK vaza para o use
 * case, o controller ou o domínio.
 */
public interface SagaCommandPublisherPort {

    void publish(SagaMessage message);
}

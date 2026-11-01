package com.os.workshop.application.saga;

import com.os.workshop.application.saga.port.out.SagaCommandPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Ponte entre o evento interno e a fila do orquestrador. Mesma forma do
 * {@code BudgetEventListener}, que já existe no projeto.
 *
 * <p>{@code AFTER_COMMIT} garante que o comando só sai depois de a mudança de
 * estado estar persistida: o {@code UpdateOrderUseCase} e o
 * {@code DecideOrderUseCase} são {@code @Transactional}, e um envio dentro da
 * transação poderia ser entregue e depois sofrer rollback.
 *
 * <p>{@code fallbackExecution = true} é necessário porque o
 * {@code CreateOrderUseCase} <em>não</em> é transacional — sem isso o Spring
 * descartaria silenciosamente o evento de criação, que é justamente o que inicia
 * a saga.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSagaEventListener {

    private final SagaActionResolver resolver;
    private final SagaCommandPublisherPort publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOrderSagaEvent(OrderSagaEvent event) {
        resolver.resolve(event).ifPresentOrElse(
                action -> publisher.publish(new OrderSagaCommand(event.orderId(), action)),
                () -> log.debug("Transicao {} -> {} da ordem {} nao corresponde a nenhuma acao da saga.",
                        event.previousStatus(), event.newStatus(), event.orderId()));
    }
}

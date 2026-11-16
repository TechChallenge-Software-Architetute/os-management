package com.os.workshop.application.saga;

import com.os.workshop.application.budget.port.out.BudgetRepository;
import com.os.workshop.application.saga.port.out.SagaCommandPublisherPort;
import com.os.workshop.domain.budget.Budget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Ponte entre o evento interno e a fila do orquestrador. Mesma forma do
 * {@code BudgetEventListener}, que já existe no projeto.
 *
 * <p>{@code AFTER_COMMIT} garante que o comando só sai depois de a mudança de
 * estado estar persistida: os use cases que publicam aqui são
 * {@code @Transactional}, e um envio dentro da transação poderia ser entregue e
 * depois sofrer rollback.
 *
 * <p>{@code fallbackExecution = true} fica como rede de segurança: se algum
 * caminho novo publicar fora de uma transação, o Spring executa o listener na
 * hora em vez de descartar o evento em silêncio.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSagaEventListener {

    private final SagaActionResolver resolver;
    private final SagaCommandPublisherPort publisher;
    private final BudgetRepository budgetRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOrderSagaEvent(OrderSagaEvent event) {
        resolver.resolve(event).ifPresentOrElse(
                action -> send(event.orderId(), action),
                () -> log.debug("Transicao {} -> {} da ordem {} nao corresponde a nenhuma acao da saga.",
                        event.previousStatus(), event.newStatus(), event.orderId()));
    }

    /**
     * Resposta a um comando do orquestrador ({@code EXECUTAR_SERVICO},
     * {@code ATUALIZAR_ESTOQUE}), publicada pelo {@link HandleSagaCommandUseCase}.
     * Mesmo motivo do metodo acima: so sai depois do commit.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onSagaReply(OrderSagaCommand reply) {
        send(reply.orderId(), reply.action());
    }

    /**
     * Monta a mensagem com o total do orcamento da OS neste momento (zero se ainda
     * nao ha orcamento, como na abertura) e o correlationId da saga.
     */
    private void send(UUID orderId, SagaAction action) {
        BigDecimal amount = budgetRepository.findByServiceOrderId(orderId)
                .map(Budget::getTotalPrice)
                .orElse(BigDecimal.ZERO);
        publisher.publish(new SagaMessage(orderId, action, amount, SagaCorrelation.current()));
    }
}

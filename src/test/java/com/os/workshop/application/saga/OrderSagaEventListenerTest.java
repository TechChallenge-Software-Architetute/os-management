package com.os.workshop.application.saga;

import com.os.workshop.application.budget.port.out.BudgetRepository;
import com.os.workshop.application.saga.port.out.SagaCommandPublisherPort;
import com.os.workshop.domain.budget.Budget;
import com.os.workshop.infrastructure.monitoring.RequestCorrelationFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderSagaEventListenerTest {

    @Mock private SagaCommandPublisherPort publisher;
    @Mock private BudgetRepository budgetRepository;

    private OrderSagaEventListener listener;
    private final UUID orderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // Resolver real: o mapa estado -> acao faz parte do comportamento testado.
        listener = new OrderSagaEventListener(new SagaActionResolver(), publisher, budgetRepository);
    }

    @AfterEach
    void limpaMdc() {
        MDC.clear();
    }

    @Test
    void aberturaEnviaCriarOsComValorZeroEOIdDaRequisicao() {
        MDC.put(RequestCorrelationFilter.REQUEST_ID, "req-123");
        when(budgetRepository.findByServiceOrderId(orderId)).thenReturn(Optional.empty());

        listener.onOrderSagaEvent(OrderSagaEvent.created(orderId, "RECEBIDA"));

        verify(publisher).publish(new SagaMessage(orderId, SagaAction.CRIAR_OS, BigDecimal.ZERO, "req-123"));
    }

    @Test
    void transicaoDeStatusNaoEnviaNada() {
        listener.onOrderSagaEvent(OrderSagaEvent.advanced(orderId, "EM_DIAGNOSTICO", "AGUARDANDO_APROVACAO"));

        verifyNoInteractions(publisher, budgetRepository);
    }

    @Test
    void respostaLevaOTotalDoOrcamentoEOCorrelationIdDaSaga() {
        MDC.put(RequestCorrelationFilter.REQUEST_ID, "req-123");
        MDC.put(SagaCorrelation.MDC_KEY, "saga-1");
        var budget = new Budget();
        budget.setTotalPrice(new BigDecimal("179.8"));
        when(budgetRepository.findByServiceOrderId(orderId)).thenReturn(Optional.of(budget));

        listener.onSagaReply(new OrderSagaCommand(orderId, SagaAction.ATUALIZAR_ESTOQUE));

        verify(publisher).publish(
                new SagaMessage(orderId, SagaAction.ATUALIZAR_ESTOQUE, new BigDecimal("179.80"), "saga-1"));
    }
}

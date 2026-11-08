package com.os.workshop.application.saga;

import com.os.workshop.application.saga.port.out.SagaCommandPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OrderSagaEventListenerTest {

    @Mock private SagaCommandPublisherPort publisher;

    private OrderSagaEventListener listener;
    private final UUID orderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // Resolver real: o mapa estado -> acao faz parte do comportamento testado.
        listener = new OrderSagaEventListener(new SagaActionResolver(), publisher);
    }

    @Test
    void enviaCriarOsQuandoAOsEAberta() {
        listener.onOrderSagaEvent(OrderSagaEvent.created(orderId, "RECEBIDA"));

        verify(publisher).publish(new OrderSagaCommand(orderId, SagaAction.CRIAR_OS));
    }

    @Test
    void transicaoDeStatusNaoEnviaNada() {
        listener.onOrderSagaEvent(OrderSagaEvent.advanced(orderId, "EM_DIAGNOSTICO", "AGUARDANDO_APROVACAO"));

        verifyNoInteractions(publisher);
    }

    @Test
    void respostaAoOrquestradorEEnviadaComoVeio() {
        var reply = new OrderSagaCommand(orderId, SagaAction.ATUALIZAR_ESTOQUE);

        listener.onSagaReply(reply);

        verify(publisher).publish(reply);
    }
}

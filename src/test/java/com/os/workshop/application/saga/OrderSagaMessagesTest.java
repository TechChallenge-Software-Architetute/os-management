package com.os.workshop.application.saga;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderSagaMessagesTest {

    private final UUID orderId = UUID.randomUUID();

    @Test
    void eventoDeCriacaoNaoTemStatusAnterior() {
        var event = OrderSagaEvent.created(orderId, "RECEBIDA");

        assertThat(event.isCreation()).isTrue();
        assertThat(event.previousStatus()).isNull();
        assertThat(event.newStatus()).isEqualTo("RECEBIDA");
        assertThat(event.orderId()).isEqualTo(orderId);
    }

    @Test
    void eventoDeAvancoGuardaOsDoisStatus() {
        var event = OrderSagaEvent.advanced(orderId, "RECEBIDA", "EM_DIAGNOSTICO");

        assertThat(event.isCreation()).isFalse();
        assertThat(event.previousStatus()).isEqualTo("RECEBIDA");
        assertThat(event.newStatus()).isEqualTo("EM_DIAGNOSTICO");
    }

    @Test
    void comandoExigeOrdem() {
        assertThatThrownBy(() -> new OrderSagaCommand(null, SagaAction.CRIAR_OS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId");
    }

    @Test
    void comandoExigeAcao() {
        assertThatThrownBy(() -> new OrderSagaCommand(orderId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("action");
    }

    @Test
    void comandoRecusadoGuardaOrdemEAcao() {
        var exception = new SagaCommandRejectedException(orderId, SagaAction.EXECUTAR_SERVICO, "fora de ordem");

        assertThat(exception.getOrderId()).isEqualTo(orderId);
        assertThat(exception.getAction()).isEqualTo(SagaAction.EXECUTAR_SERVICO);
        assertThat(exception.getMessage()).contains("EXECUTAR_SERVICO", orderId.toString(), "fora de ordem");
    }
}

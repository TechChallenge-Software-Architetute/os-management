package com.os.workshop.application.saga;

import com.os.workshop.infrastructure.monitoring.RequestCorrelationFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SagaMessageTest {

    private final UUID orderId = UUID.randomUUID();

    @AfterEach
    void limpaMdc() {
        MDC.clear();
    }

    @Test
    void valorSempreComDuasCasasEZeroQuandoNaoHaOrcamento() {
        assertThat(new SagaMessage(orderId, SagaAction.CRIAR_OS, null, "c").amount()).isEqualTo("0.00");
        assertThat(new SagaMessage(orderId, SagaAction.CRIAR_OS, new BigDecimal("179.8"), "c").amount())
                .isEqualTo("179.80");
    }

    @Test
    void exigeOrdemAcaoECorrelationId() {
        assertThatThrownBy(() -> new SagaMessage(null, SagaAction.CRIAR_OS, BigDecimal.ONE, "c"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("orderId");
        assertThatThrownBy(() -> new SagaMessage(orderId, null, BigDecimal.ONE, "c"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("action");
        assertThatThrownBy(() -> new SagaMessage(orderId, SagaAction.CRIAR_OS, BigDecimal.ONE, " "))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("correlationId");
    }

    @Test
    void correlationIdDaSagaTemPrioridadeSobreODaRequisicao() {
        MDC.put(RequestCorrelationFilter.REQUEST_ID, "req-1");
        MDC.put(SagaCorrelation.MDC_KEY, "saga-1");

        assertThat(SagaCorrelation.current()).isEqualTo("saga-1");
    }

    @Test
    void semSagaUsaOIdDaRequisicao() {
        MDC.put(RequestCorrelationFilter.REQUEST_ID, "req-1");

        assertThat(SagaCorrelation.current()).isEqualTo("req-1");
    }

    @Test
    void semNenhumContextoGeraUmUuid() {
        String generated = SagaCorrelation.current();

        assertThatNoException().isThrownBy(() -> UUID.fromString(generated));
    }
}

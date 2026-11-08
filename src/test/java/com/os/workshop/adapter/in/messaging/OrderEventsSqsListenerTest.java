package com.os.workshop.adapter.in.messaging;

import com.os.workshop.application.saga.HandleSagaCommandUseCase;
import com.os.workshop.application.saga.SagaAction;
import com.os.workshop.application.saga.SagaCorrelation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OrderEventsSqsListenerTest {

    @Mock private HandleSagaCommandUseCase handleSagaCommandUseCase;
    @InjectMocks private OrderEventsSqsListener listener;

    private final UUID orderId = UUID.randomUUID();

    @Test
    void corpoJsonEntregaOrdemEAcaoEPropagaOCorrelationId() {
        var correlationDuringExecution = new AtomicReference<String>();
        doAnswer(invocation -> {
            correlationDuringExecution.set(MDC.get(SagaCorrelation.MDC_KEY));
            return null;
        }).when(handleSagaCommandUseCase).execute(orderId, SagaAction.EXECUTAR_SERVICO);

        listener.onMessage("{\"order\":\"" + orderId + "\",\"amount\":179.80,\"correlationId\":\"saga-1\"}",
                "EXECUTAR_SERVICO");

        verify(handleSagaCommandUseCase).execute(orderId, SagaAction.EXECUTAR_SERVICO);
        assertThat(correlationDuringExecution.get()).isEqualTo("saga-1");
        assertThat(MDC.get(SagaCorrelation.MDC_KEY)).isNull();
    }

    @Test
    void acaoVemDoCampoDoJsonMesmoSemOAtributo() {
        listener.onMessage("{\"order\":\"" + orderId + "\",\"action\":\"ATUALIZAR_ESTOQUE\","
                + "\"amount\":179.80,\"correlationId\":\"saga-1\"}", null);

        verify(handleSagaCommandUseCase).execute(orderId, SagaAction.ATUALIZAR_ESTOQUE);
    }

    @Test
    void campoActionDoJsonPrevaleceSobreOAtributo() {
        listener.onMessage("{\"order\":\"" + orderId + "\",\"action\":\"NEGAR_SERVICO\"}", "EXECUTAR_SERVICO");

        verify(handleSagaCommandUseCase).execute(orderId, SagaAction.NEGAR_SERVICO);
    }

    @Test
    void jsonSemAcaoESemAtributoERecusado() {
        assertThatThrownBy(() -> listener.onMessage("{\"order\":\"" + orderId + "\"}", null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(handleSagaCommandUseCase);
    }

    @Test
    void acaoDesconhecidaNoJsonERecusada() {
        assertThatThrownBy(() -> listener.onMessage(
                "{\"order\":\"" + orderId + "\",\"action\":\"PAGAR_TUDO\"}", null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(handleSagaCommandUseCase);
    }

    @Test
    void corpoJsonSemCorrelationIdTambemFunciona() {
        listener.onMessage("{\"order\":\"" + orderId + "\"}", "ATUALIZAR_ESTOQUE");

        verify(handleSagaCommandUseCase).execute(orderId, SagaAction.ATUALIZAR_ESTOQUE);
    }

    @Test
    void formatoAntigoSoComOIdContinuaAceito() {
        listener.onMessage(orderId.toString(), "EXECUTAR_SERVICO");

        verify(handleSagaCommandUseCase).execute(orderId, SagaAction.EXECUTAR_SERVICO);
    }

    @Test
    void toleraEspacosEIdEntreAspas() {
        listener.onMessage("  \"" + orderId + "\"  ", " ATUALIZAR_ESTOQUE ");

        verify(handleSagaCommandUseCase).execute(orderId, SagaAction.ATUALIZAR_ESTOQUE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "order-4444", "\"\"", "{\"amount\":10}", "{\"order\":\"nao-e-uuid\"}",
            "{\"order\":null}", "{json quebrado"})
    void corpoSemIdValidoERecusado(String body) {
        assertThatThrownBy(() -> listener.onMessage(body, "EXECUTAR_SERVICO"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(handleSagaCommandUseCase);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "ACAO_QUE_NAO_EXISTE", "executar_servico"})
    void acaoAusenteOuDesconhecidaERecusada(String action) {
        assertThatThrownBy(() -> listener.onMessage(orderId.toString(), action))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(handleSagaCommandUseCase);
    }

    @Test
    void erroDoUseCaseSobeELimpaOContextoDeLog() {
        doThrow(new IllegalStateException("falhou"))
                .when(handleSagaCommandUseCase).execute(eq(orderId), any(SagaAction.class));

        assertThatThrownBy(() -> listener.onMessage(
                "{\"order\":\"" + orderId + "\",\"correlationId\":\"saga-2\"}", "NEGAR_SERVICO"))
                .isInstanceOf(IllegalStateException.class);

        assertThat(MDC.get("orderId")).isNull();
        assertThat(MDC.get("sagaAction")).isNull();
        assertThat(MDC.get(SagaCorrelation.MDC_KEY)).isNull();
    }
}

package com.os.workshop.application.saga;

import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SagaActionResolverTest {

    private final SagaActionResolver resolver = new SagaActionResolver();
    private final UUID orderId = UUID.randomUUID();

    @Test
    void aberturaDaOsIniciaASaga() {
        var event = OrderSagaEvent.created(orderId, OrderServiceStatusEnum.RECEBIDA.name());

        assertThat(resolver.resolve(event)).contains(SagaAction.CRIAR_OS);
    }

    @ParameterizedTest
    @EnumSource(value = OrderServiceStatusEnum.class, names = {"APROVADO"})
    void aprovacaoDoClienteNotificaAOrquestracao(OrderServiceStatusEnum target) {
        var event = OrderSagaEvent.advanced(orderId, "AGUARDANDO_APROVACAO", target.name());

        assertThat(resolver.resolve(event)).contains(SagaAction.ORCAMENTO_APROVADO);
    }

    @ParameterizedTest
    @EnumSource(value = OrderServiceStatusEnum.class, mode = EnumSource.Mode.EXCLUDE, names = {"APROVADO"})
    void transicaoDeStatusNaoGeraComando(OrderServiceStatusEnum target) {
        var event = OrderSagaEvent.advanced(orderId, "QUALQUER", target.name());

        assertThat(resolver.resolve(event)).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "STATUS_INEXISTENTE"})
    void statusAusenteOuDesconhecidoNaoDerrubaOFluxo(String newStatus) {
        var event = OrderSagaEvent.advanced(orderId, OrderServiceStatusEnum.RECEBIDA.name(), newStatus);

        assertThat(resolver.resolve(event)).isEmpty();
    }
}

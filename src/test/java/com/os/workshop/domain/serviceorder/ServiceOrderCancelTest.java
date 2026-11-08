package com.os.workshop.domain.serviceorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceOrderCancelTest {

    @ParameterizedTest
    @EnumSource(value = OrderServiceStatusEnum.class,
            names = {"RECEBIDA", "EM_DIAGNOSTICO", "AGUARDANDO_APROVACAO", "APROVADO"})
    void cancelaAntesDaExecucaoEGuardaOMotivo(OrderServiceStatusEnum status) {
        var order = orderIn(status);

        order.cancel("Servico negado pela saga");

        assertThat(order.getStatus()).isEqualTo(OrderServiceStatusEnum.CANCELADA);
        assertThat(order.getRejectionReason()).isEqualTo("Servico negado pela saga");
    }

    @ParameterizedTest
    @EnumSource(value = OrderServiceStatusEnum.class,
            names = {"EM_EXECUCAO", "FINALIZADA", "ENTREGUE", "RECUSADA", "CANCELADA"})
    void naoCancelaDepoisDaExecucaoNemOsJaEncerrada(OrderServiceStatusEnum status) {
        var order = orderIn(status);

        assertThatThrownBy(() -> order.cancel("qualquer"))
                .isInstanceOf(InvalidStatusTransitionException.class);
        assertThat(order.getStatus()).isEqualTo(status);
    }

    @Test
    void canceladaEEstadoFinal() {
        assertThat(OrderServiceStatusEnum.CANCELADA.getAllowedTransitions()).isEmpty();
        assertThat(OrderStatusLabel.of(OrderServiceStatusEnum.CANCELADA)).isEqualTo("Cancelada");
    }

    private ServiceOrder orderIn(OrderServiceStatusEnum status) {
        return ServiceOrder.reconstitute(UUID.randomUUID(), "52998224725", "BDD1A23", List.of("TROCA_OLEO"),
                "[TROCA_OLEO]", status, null, LocalDateTime.now(), LocalDateTime.now());
    }
}

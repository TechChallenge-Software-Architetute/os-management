package com.os.workshop.domain.serviceorder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum OrderServiceStatusEnum {
    RECEBIDA("RECEBIDA"),
    EM_DIAGNOSTICO("EM_DIAGNOSTICO"),
    AGUARDANDO_APROVACAO("AGUARDANDO_APROVACAO"),
    APROVADO("APROVADO"),
    EM_EXECUCAO("EM_EXECUCAO"),
    FINALIZADA("FINALIZADA"),
    ENTREGUE("ENTREGUE"),
    RECUSADA("RECUSADA"),
    /** Encerrada pela compensacao da saga (NEGAR_SERVICO, NEGAR_ESTOQUE ou ERRO). Estado final. */
    CANCELADA("CANCELADA");

    private final String status;

    // CANCELADA so e alcancavel antes da execucao: depois que o servico comeca, a
    // saga nao desfaz a OS automaticamente.
    private static final Map<OrderServiceStatusEnum, List<OrderServiceStatusEnum>> ALLOWED_TRANSITIONS = Map.of(
            RECEBIDA, List.of(EM_DIAGNOSTICO, CANCELADA),
            EM_DIAGNOSTICO, List.of(AGUARDANDO_APROVACAO, CANCELADA),
            AGUARDANDO_APROVACAO, List.of(APROVADO, RECUSADA, CANCELADA),
            APROVADO, List.of(EM_EXECUCAO, CANCELADA),
            EM_EXECUCAO, List.of(FINALIZADA),
            FINALIZADA, List.of(ENTREGUE),
            RECUSADA, List.of(EM_DIAGNOSTICO)
    );

    public List<OrderServiceStatusEnum> getAllowedTransitions() {
        return ALLOWED_TRANSITIONS.getOrDefault(this, List.of());
    }

    public boolean canTransitionTo(OrderServiceStatusEnum target) {
        return getAllowedTransitions().contains(target);
    }
}

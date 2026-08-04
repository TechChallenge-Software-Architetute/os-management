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
    RECUSADA("RECUSADA");

    private final String status;

    private static final Map<OrderServiceStatusEnum, List<OrderServiceStatusEnum>> ALLOWED_TRANSITIONS = Map.of(
            RECEBIDA, List.of(EM_DIAGNOSTICO),
            EM_DIAGNOSTICO, List.of(AGUARDANDO_APROVACAO),
            AGUARDANDO_APROVACAO, List.of(APROVADO, RECUSADA),
            APROVADO, List.of(EM_EXECUCAO),
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

package com.os.workshop.features.serviceorder.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum OrderServiceStatusEnum {
    RECEBIDA("RECEBIDA"),
    EM_DIAGNOSTICO("EM_DIAGNOSTICO"),
    AGUARDANDO_APROVACAO("AGUARDANDO_APROVACAO"),
    APROVADO("APROVADO"),
    EM_EXECUCAO("EM_EXECUCAO"),
    FINALIZADA("FINALIZADA"),
    ENTREGUE("ENTREGUE");

    private final String status;
}

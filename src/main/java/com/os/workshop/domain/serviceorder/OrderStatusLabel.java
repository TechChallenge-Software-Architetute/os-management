package com.os.workshop.domain.serviceorder;

import java.util.Map;

public final class OrderStatusLabel {

    private OrderStatusLabel() {}

    private static final Map<OrderServiceStatusEnum, String> LABELS = Map.of(
            OrderServiceStatusEnum.RECEBIDA, "Recebida",
            OrderServiceStatusEnum.EM_DIAGNOSTICO, "Em Diagnostico",
            OrderServiceStatusEnum.AGUARDANDO_APROVACAO, "Aguardando Aprovacao",
            OrderServiceStatusEnum.APROVADO, "Aprovado",
            OrderServiceStatusEnum.EM_EXECUCAO, "Em Execucao",
            OrderServiceStatusEnum.FINALIZADA, "Finalizada",
            OrderServiceStatusEnum.ENTREGUE, "Entregue",
            OrderServiceStatusEnum.RECUSADA, "Recusada"
    );

    public static String of(OrderServiceStatusEnum status) {
        return LABELS.getOrDefault(status, status.getStatus());
    }

    public static String of(String statusValue) {
        try {
            return of(OrderServiceStatusEnum.valueOf(statusValue));
        } catch (IllegalArgumentException e) {
            return statusValue;
        }
    }
}

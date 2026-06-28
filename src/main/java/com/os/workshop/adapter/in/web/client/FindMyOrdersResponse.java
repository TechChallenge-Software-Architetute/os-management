package com.os.workshop.adapter.in.web.client;

import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;

import java.util.List;
import java.util.UUID;

public record FindMyOrdersResponse(
        UUID orderId,
        String placaVeiculo,
        String serviceStatus,
        List<String> services
) {
    public static FindMyOrdersResponse from(ServiceOrderEntity entity) {
        return new FindMyOrdersResponse(
                entity.getId(),
                entity.getPlacaVeiculo(),
                entity.getServiceStatus(),
                entity.getListService()
        );
    }
}

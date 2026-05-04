package com.os.workshop.features.client.myOrders;

import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;

import java.util.List;
import java.util.UUID;

/**
 * Summary view of a service order for the client portal.
 * Contains only basic information without service-level details.
 */
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

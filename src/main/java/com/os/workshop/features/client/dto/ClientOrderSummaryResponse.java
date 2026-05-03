package com.os.workshop.features.client.dto;

import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;

import java.util.List;
import java.util.UUID;

/**
 * Summary view of a service order for the client portal.
 * Contains only basic information without service-level details.
 */
public record ClientOrderSummaryResponse(
        UUID orderId,
        String placaVeiculo,
        String serviceStatus,
        List<String> services
) {
    public static ClientOrderSummaryResponse from(ServiceOrderEntity entity) {
        return new ClientOrderSummaryResponse(
                entity.getId(),
                entity.getPlacaVeiculo(),
                entity.getServiceStatus(),
                entity.getListService()
        );
    }
}

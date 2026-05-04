package com.os.workshop.features.client.myOrders;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;

import java.util.List;
import java.util.UUID;

/**
 * Summary view of a service order for the client portal.
 * Contains only basic information without service-level details.
 */
@Schema(description = "Find My Orders response payload.")
public record FindMyOrdersResponse(
        @Schema(description = "Order identifier.", example = "11111111-1111-1111-1111-111111111111") UUID orderId,
        @Schema(description = "Placa Veiculo.", example = "ABC-1234") String placaVeiculo,
        @Schema(description = "Service Status.", example = "RECEBIDA") String serviceStatus,
        @Schema(description = "Services.", example = "example") List<String> services
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

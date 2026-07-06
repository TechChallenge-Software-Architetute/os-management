package com.os.workshop.adapter.in.web.client;

import com.os.workshop.domain.serviceorder.ServiceOrder;

import java.util.List;
import java.util.UUID;

public record FindMyOrdersResponse(
        UUID orderId, String placaVeiculo, String serviceStatus, List<String> services
) {
    public static FindMyOrdersResponse from(ServiceOrder order) {
        return new FindMyOrdersResponse(order.getId(), order.getPlacaVeiculo(),
                order.getServiceStatus(), order.getListService());
    }
}

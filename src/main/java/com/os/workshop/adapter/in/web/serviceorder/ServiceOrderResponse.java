package com.os.workshop.adapter.in.web.serviceorder;

import com.os.workshop.domain.serviceorder.ServiceOrder;

import java.util.List;
import java.util.UUID;

public record ServiceOrderResponse(
        UUID id, String serviceTypeName, String serviceStatus,
        List<String> listService, String cpfCnpj, String placaVeiculo
) {
    public static ServiceOrderResponse from(ServiceOrder order) {
        return new ServiceOrderResponse(order.getId(), order.getServiceTypeName(),
                order.getServiceStatus(), order.getListService(),
                order.getCpfCnpj(), order.getPlacaVeiculo());
    }
}

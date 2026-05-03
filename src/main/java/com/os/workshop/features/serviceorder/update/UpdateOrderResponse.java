package com.os.workshop.features.serviceorder.update;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderResponse;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;

import java.util.List;
import java.util.UUID;

public record UpdateOrderResponse(
        UUID id,
        String serviceTypeName,
        String serviceStatus,
        List<String> listService,
        String cpfCnpj,
        String placaVeiculo,
        FindBudgetByServiceOrderResponse budget
) {
    public static UpdateOrderResponse from(ServiceOrder order) {
        return new UpdateOrderResponse(
                order.getId(),
                order.getServiceTypeName(),
                order.getServiceStatus(),
                order.getListService(),
                order.getCpfCnpj(),
                order.getPlacaVeiculo(),
                order.getBudget()
        );
    }
}

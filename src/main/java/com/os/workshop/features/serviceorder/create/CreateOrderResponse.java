package com.os.workshop.features.serviceorder.create;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderResponse;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;

import java.util.List;
import java.util.UUID;

public record CreateOrderResponse(
        UUID id,
        String serviceTypeName,
        String serviceStatus,
        List<String> listService,
        String cpfCnpj,
        String placaVeiculo,
        FindBudgetByServiceOrderResponse budget
) {
    public static CreateOrderResponse from(ServiceOrder order) {
        return new CreateOrderResponse(
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

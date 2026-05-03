package com.os.workshop.features.serviceorder.list;

import com.os.workshop.features.budget.BudgetResponse;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;

import java.util.List;
import java.util.UUID;

public record ListOrdersResponse(
        UUID id,
        String serviceTypeName,
        String serviceStatus,
        List<String> listService,
        String cpfCnpj,
        String placaVeiculo,
        BudgetResponse budget
) {
    public static ListOrdersResponse from(ServiceOrder order) {
        return new ListOrdersResponse(
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

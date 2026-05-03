package com.os.workshop.features.serviceorder.findById;

import com.os.workshop.features.budget.BudgetResponse;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;

import java.util.List;
import java.util.UUID;

public record FindOrderByIdResponse(
        UUID id,
        String serviceTypeName,
        String serviceStatus,
        List<String> listService,
        String cpfCnpj,
        String placaVeiculo,
        BudgetResponse budget
) {
    public static FindOrderByIdResponse from(ServiceOrder order) {
        return new FindOrderByIdResponse(
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

package com.os.workshop.features.serviceorder.update;

import com.os.workshop.features.budget.BudgetResponse;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Service order data returned after an update.")
public record UpdateOrderResponse(
        @Schema(description = "Service order unique identifier.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601")
        UUID id,
        @Schema(description = "Primary service type name.", example = "OIL_CHANGE")
        String serviceTypeName,
        @Schema(description = "Current service order status.", example = "IN_PROGRESS")
        String serviceStatus,
        @Schema(description = "Service types requested for the order.", example = "[\"OIL_CHANGE\", \"ALIGNMENT\"]")
        List<String> listService,
        @Schema(description = "Customer CPF or CNPJ.", example = "123.456.789-09")
        String cpfCnpj,
        @Schema(description = "Vehicle license plate.", example = "ABC-1234")
        String placaVeiculo,
        @Schema(description = "Budget generated for the service order.")
        BudgetResponse budget
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

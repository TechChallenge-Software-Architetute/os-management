package com.os.workshop.features.serviceorder.findById;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderResponse;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;

import java.util.List;
import java.util.UUID;

@Schema(description = "Find Order By Id response payload.")
public record FindOrderByIdResponse(
        @Schema(description = "Identifier.", example = "1") UUID id,
        @Schema(description = "Service Type Name.", example = "TROCA_OLEO") String serviceTypeName,
        @Schema(description = "Service Status.", example = "RECEBIDA") String serviceStatus,
        @Schema(description = "List Service.", example = "TROCA_OLEO") List<String> listService,
        @Schema(description = "CPF Cnpj.", example = "529.982.247-25") String cpfCnpj,
        @Schema(description = "Placa Veiculo.", example = "ABC-1234") String placaVeiculo,
        @Schema(description = "Budget.", example = "{}") FindBudgetByServiceOrderResponse budget
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

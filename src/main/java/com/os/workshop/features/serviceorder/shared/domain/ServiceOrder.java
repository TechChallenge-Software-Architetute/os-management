package com.os.workshop.features.serviceorder.shared.domain;

import com.os.workshop.features.budget.BudgetResponse;
import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * Domain class representing a service order with its associated budget.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOrder {

    private UUID id;
    private String serviceTypeName;
    private String serviceStatus;
    private List<String> listService;
    private String cpfCnpj;
    private String placaVeiculo;
    private BudgetResponse budget;
}

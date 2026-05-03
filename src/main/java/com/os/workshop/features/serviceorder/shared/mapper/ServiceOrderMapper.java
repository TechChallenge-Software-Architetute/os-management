package com.os.workshop.features.serviceorder.shared.mapper;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderResponse;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;

/**
 * Manual mapper between ServiceOrderEntity (JPA) and ServiceOrder (domain).
 */
public class ServiceOrderMapper {

    private ServiceOrderMapper() {
    }

    public static ServiceOrder toDomain(ServiceOrderEntity entity, FindBudgetByServiceOrderResponse budget) {
        if (entity == null) {
            return null;
        }
        return ServiceOrder.builder()
                .id(entity.getId())
                .serviceTypeName(entity.getServiceTypeName())
                .serviceStatus(entity.getServiceStatus())
                .listService(entity.getListService())
                .cpfCnpj(entity.getCpfCnpj())
                .placaVeiculo(entity.getPlacaVeiculo())
                .budget(budget)
                .build();
    }

    public static ServiceOrder toDomain(ServiceOrderEntity entity) {
        return toDomain(entity, null);
    }

    public static ServiceOrderEntity toEntity(ServiceOrder domain) {
        if (domain == null) {
            return null;
        }
        return ServiceOrderEntity.builder()
                .id(domain.getId())
                .serviceTypeName(domain.getServiceTypeName())
                .serviceStatus(domain.getServiceStatus())
                .listService(domain.getListService())
                .cpfCnpj(domain.getCpfCnpj())
                .placaVeiculo(domain.getPlacaVeiculo())
                .build();
    }
}

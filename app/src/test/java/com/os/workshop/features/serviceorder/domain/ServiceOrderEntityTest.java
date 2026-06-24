package com.os.workshop.features.serviceorder.domain;

import com.os.workshop.features.serviceorder.shared.domain.enums.OrderServiceStatusEnum;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ServiceOrderEntityTest {

    @Test
    void builderCreatesServiceOrder() {
        UUID id = UUID.randomUUID();

        ServiceOrderEntity entity = ServiceOrderEntity.builder()
                .id(id)
                .cpfCnpj("52998224725")
                .placaVeiculo("ABC1234")
                .serviceTypeName("REVISAO")
                .serviceStatus(OrderServiceStatusEnum.RECEBIDA.getStatus())
                .listService(List.of("REVISAO"))
                .build();

        assertEquals(id, entity.getId());
        assertEquals(OrderServiceStatusEnum.RECEBIDA.getStatus(), entity.getServiceStatus());
        assertFalse(entity.getListService().isEmpty());
    }
}

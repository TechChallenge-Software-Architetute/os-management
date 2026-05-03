package com.os.workshop.features.serviceorder.list;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderHandler;
import com.os.workshop.features.serviceorder.shared.domain.enums.OrderServiceStatusEnum;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListOrdersHandlerTest {

    @Mock
    private ServiceOrderJpaRepository serviceOrderJpaRepository;

    @Mock
    private FindBudgetByServiceOrderHandler findBudgetByServiceOrderHandler;

    @InjectMocks
    private ListOrdersHandler listOrdersHandler;

    private ServiceOrderEntity createOrder(UUID id) {
        ServiceOrderEntity order = new ServiceOrderEntity();
        order.setId(id);
        order.setServiceTypeName("[TROCA_OLEO]");
        order.setServiceStatus(OrderServiceStatusEnum.RECEBIDA.getStatus());
        order.setListService(List.of("TROCA_OLEO"));
        order.setCpfCnpj("12345678900");
        order.setPlacaVeiculo("ABC1234");
        return order;
    }

    @Test
    void whenConsultingAllOrders_thenReturnsOrderList() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<ServiceOrderEntity> orders = List.of(createOrder(id1), createOrder(id2));

        when(serviceOrderJpaRepository.findAll()).thenReturn(orders);
        when(findBudgetByServiceOrderHandler.handle(id1)).thenReturn(Optional.empty());
        when(findBudgetByServiceOrderHandler.handle(id2)).thenReturn(Optional.empty());

        List<ServiceOrder> result = listOrdersHandler.handle();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(serviceOrderJpaRepository).findAll();
    }

    @Test
    void whenConsultingAllOrdersWithEmptyDatabase_thenReturnsEmptyList() {
        when(serviceOrderJpaRepository.findAll()).thenReturn(Collections.emptyList());

        List<ServiceOrder> result = listOrdersHandler.handle();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

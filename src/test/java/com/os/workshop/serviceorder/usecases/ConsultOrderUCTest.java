package com.os.workshop.serviceorder.usecases;

import com.os.workshop.serviceorder.adapter.database.OrderRepository;
import com.os.workshop.serviceorder.domain.OrderServiceStatusEnum;
import com.os.workshop.serviceorder.domain.ServiceOrderEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultOrderUCTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ConsultOrderUC consultOrderUC;

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

        when(orderRepository.findAll()).thenReturn(orders);

        List<ServiceOrderEntity> result = consultOrderUC.process();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void whenConsultingAllOrdersWithEmptyDatabase_thenReturnsEmptyList() {
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        List<ServiceOrderEntity> result = consultOrderUC.process();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenConsultingOrderByExistingId_thenReturnsOrder() {
        UUID orderId = UUID.randomUUID();
        ServiceOrderEntity expected = createOrder(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(expected));

        ServiceOrderEntity result = consultOrderUC.process(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals("12345678900", result.getCpfCnpj());
        assertEquals("ABC1234", result.getPlacaVeiculo());
        assertEquals(OrderServiceStatusEnum.RECEBIDA.getStatus(), result.getServiceStatus());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void whenConsultingOrderByNonExistingId_thenThrowsRuntimeException() {
        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> consultOrderUC.process(orderId));

        assertEquals("Ordem de servico nao encontrada. ID: " + orderId, exception.getMessage());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void whenConsultingOrderByExistingId_thenReturnsCorrectServiceList() {
        UUID orderId = UUID.randomUUID();
        ServiceOrderEntity expected = createOrder(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(expected));

        ServiceOrderEntity result = consultOrderUC.process(orderId);

        assertEquals(List.of("TROCA_OLEO"), result.getListService());
    }
}

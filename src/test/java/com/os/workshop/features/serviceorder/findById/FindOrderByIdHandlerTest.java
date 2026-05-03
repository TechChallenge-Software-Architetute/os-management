package com.os.workshop.features.serviceorder.findById;

import com.os.workshop.features.budget.BudgetService;
import com.os.workshop.features.serviceorder.shared.domain.enums.OrderServiceStatusEnum;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindOrderByIdHandlerTest {

    @Mock
    private ServiceOrderJpaRepository serviceOrderJpaRepository;

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private FindOrderByIdHandler findOrderByIdHandler;

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
    void whenConsultingOrderByExistingId_thenReturnsOrder() {
        UUID orderId = UUID.randomUUID();
        ServiceOrderEntity expected = createOrder(orderId);

        when(serviceOrderJpaRepository.findById(orderId)).thenReturn(Optional.of(expected));
        when(budgetService.findByServiceOrderId(orderId)).thenReturn(Optional.empty());

        ServiceOrder result = findOrderByIdHandler.handle(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals("12345678900", result.getCpfCnpj());
        assertEquals("ABC1234", result.getPlacaVeiculo());
        assertEquals(OrderServiceStatusEnum.RECEBIDA.getStatus(), result.getServiceStatus());
        verify(serviceOrderJpaRepository).findById(orderId);
    }

    @Test
    void whenConsultingOrderByNonExistingId_thenThrowsRuntimeException() {
        UUID orderId = UUID.randomUUID();

        when(serviceOrderJpaRepository.findById(orderId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> findOrderByIdHandler.handle(orderId));

        assertEquals("Ordem de servico nao encontrada. ID: " + orderId, exception.getMessage());
        verify(serviceOrderJpaRepository).findById(orderId);
    }

    @Test
    void whenConsultingOrderByExistingId_thenReturnsCorrectServiceList() {
        UUID orderId = UUID.randomUUID();
        ServiceOrderEntity expected = createOrder(orderId);

        when(serviceOrderJpaRepository.findById(orderId)).thenReturn(Optional.of(expected));
        when(budgetService.findByServiceOrderId(orderId)).thenReturn(Optional.empty());

        ServiceOrder result = findOrderByIdHandler.handle(orderId);

        assertEquals(List.of("TROCA_OLEO"), result.getListService());
    }
}

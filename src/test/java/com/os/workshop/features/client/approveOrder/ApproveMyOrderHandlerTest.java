package com.os.workshop.features.client.approveOrder;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderHandler;
import com.os.workshop.features.budget.shared.domain.Budget;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.client.ApproveMyOrderUseCase;
import com.os.workshop.features.serviceorder.shared.domain.enums.OrderServiceStatusEnum;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApproveMyOrderUseCaseTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ServiceOrderJpaRepository serviceOrderJpaRepository;

    @Mock
    private FindBudgetByServiceOrderHandler findBudgetByServiceOrderHandler;

    @InjectMocks
    private ApproveMyOrderUseCase handler;

    private static final String VALID_CPF = "12345678909";
    private static final String EMAIL = "john@email.com";

    private Client createClient() {
        return Client.create("JOHN DOE", VALID_CPF, EMAIL, null);
    }

    private ServiceOrderEntity createOrder(UUID id, String cpf, String status) {
        return ServiceOrderEntity.builder()
                .id(id)
                .cpfCnpj(cpf)
                .placaVeiculo("ABC1234")
                .serviceStatus(status)
                .listService(List.of("TROCA_OLEO"))
                .build();
    }

    @Test
    void handle_whenOrderIsAwaitingApproval_thenApprovesSuccessfully() {
        UUID orderId = UUID.randomUUID();
        Client client = createClient();
        ServiceOrderEntity order = createOrder(orderId, VALID_CPF,
                OrderServiceStatusEnum.AGUARDANDO_APROVACAO.getStatus());

        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(serviceOrderJpaRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(serviceOrderJpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Budget budget = new Budget();
        budget.setTotalPrice(BigDecimal.valueOf(250));
        when(findBudgetByServiceOrderHandler.handle(orderId)).thenReturn(Optional.of(budget));

        ApproveMyOrderUseCase.ApproveMyOrderResult result = handler.execute(EMAIL, orderId);

        assertEquals(OrderServiceStatusEnum.APROVADO.getStatus(), result.order().getServiceStatus());
        assertNotNull(result.budget());
        verify(serviceOrderJpaRepository).save(order);
    }

    @Test
    void handle_whenNoBudget_thenApprovesWithNullBudget() {
        UUID orderId = UUID.randomUUID();
        Client client = createClient();
        ServiceOrderEntity order = createOrder(orderId, VALID_CPF,
                OrderServiceStatusEnum.AGUARDANDO_APROVACAO.getStatus());

        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(serviceOrderJpaRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(serviceOrderJpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(findBudgetByServiceOrderHandler.handle(orderId)).thenReturn(Optional.empty());

        ApproveMyOrderUseCase.ApproveMyOrderResult result = handler.execute(EMAIL, orderId);

        assertEquals(OrderServiceStatusEnum.APROVADO.getStatus(), result.order().getServiceStatus());
        assertNull(result.budget());
    }

    @Test
    void handle_whenClientNotFound_thenThrowsClientNotFoundException() {
        when(clientRepository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class,
                () -> handler.execute("unknown@email.com", UUID.randomUUID()));
    }

    @Test
    void handle_whenOrderNotFound_thenThrowsIllegalArgumentException() {
        UUID orderId = UUID.randomUUID();
        Client client = createClient();
        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(serviceOrderJpaRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> handler.execute(EMAIL, orderId));
    }

    @Test
    void handle_whenOrderBelongsToDifferentClient_thenThrowsIllegalArgumentException() {
        UUID orderId = UUID.randomUUID();
        Client client = createClient();
        ServiceOrderEntity order = createOrder(orderId, "99999999999",
                OrderServiceStatusEnum.AGUARDANDO_APROVACAO.getStatus());

        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(serviceOrderJpaRepository.findById(orderId)).thenReturn(Optional.of(order));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> handler.execute(EMAIL, orderId));

        assertTrue(ex.getMessage().contains("does not belong"));
        verify(serviceOrderJpaRepository, never()).save(any());
    }

    @Test
    void handle_whenOrderNotInAwaitingApprovalStatus_thenThrowsIllegalStateException() {
        UUID orderId = UUID.randomUUID();
        Client client = createClient();
        ServiceOrderEntity order = createOrder(orderId, VALID_CPF,
                OrderServiceStatusEnum.RECEBIDA.getStatus());

        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(serviceOrderJpaRepository.findById(orderId)).thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> handler.execute(EMAIL, orderId));

        assertTrue(ex.getMessage().contains("cannot be approved"));
        verify(serviceOrderJpaRepository, never()).save(any());
    }
}

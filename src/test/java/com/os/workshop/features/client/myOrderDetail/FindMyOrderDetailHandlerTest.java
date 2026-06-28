package com.os.workshop.features.client.myOrderDetail;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderHandler;
import com.os.workshop.features.budget.shared.domain.Budget;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.client.FindMyOrderDetailUseCase;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindMyOrderDetailUseCaseTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ServiceOrderJpaRepository serviceOrderJpaRepository;

    @Mock
    private FindBudgetByServiceOrderHandler findBudgetByServiceOrderHandler;

    @InjectMocks
    private FindMyOrderDetailUseCase handler;

    private static final String VALID_CPF = "12345678909";
    private static final String EMAIL = "john@email.com";

    private Client createClient() {
        return Client.create("JOHN DOE", VALID_CPF, EMAIL, null);
    }

    private ServiceOrderEntity createOrder(UUID id, String cpf) {
        return ServiceOrderEntity.builder()
                .id(id)
                .cpfCnpj(cpf)
                .placaVeiculo("ABC1234")
                .serviceStatus("RECEBIDA")
                .listService(List.of("TROCA_OLEO"))
                .build();
    }

    @Test
    void handle_whenOrderBelongsToClient_thenReturnsDetail() {
        UUID orderId = UUID.randomUUID();
        Client client = createClient();
        ServiceOrderEntity order = createOrder(orderId, VALID_CPF);

        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(serviceOrderJpaRepository.findById(orderId)).thenReturn(Optional.of(order));

        Budget budget = new Budget();
        budget.setServiceOrderId(orderId);
        budget.setTotalPrice(BigDecimal.valueOf(100));
        when(findBudgetByServiceOrderHandler.handle(orderId)).thenReturn(Optional.of(budget));

        FindMyOrderDetailUseCase.FindMyOrderDetailResult result = handler.execute(EMAIL, orderId);

        assertNotNull(result);
        assertEquals(orderId, result.order().getId());
        assertNotNull(result.budget());
    }

    @Test
    void handle_whenNoBudgetExists_thenReturnsDetailWithNullBudget() {
        UUID orderId = UUID.randomUUID();
        Client client = createClient();
        ServiceOrderEntity order = createOrder(orderId, VALID_CPF);

        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(serviceOrderJpaRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(findBudgetByServiceOrderHandler.handle(orderId)).thenReturn(Optional.empty());

        FindMyOrderDetailUseCase.FindMyOrderDetailResult result = handler.execute(EMAIL, orderId);

        assertNotNull(result.order());
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

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> handler.execute(EMAIL, orderId));

        assertTrue(ex.getMessage().contains(orderId.toString()));
    }

    @Test
    void handle_whenOrderBelongsToDifferentClient_thenThrowsIllegalArgumentException() {
        UUID orderId = UUID.randomUUID();
        Client client = createClient();
        ServiceOrderEntity order = createOrder(orderId, "99999999999");

        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(serviceOrderJpaRepository.findById(orderId)).thenReturn(Optional.of(order));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> handler.execute(EMAIL, orderId));

        assertTrue(ex.getMessage().contains("does not belong"));
    }
}

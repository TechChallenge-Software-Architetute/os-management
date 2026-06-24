package com.os.workshop.features.client.myOrders;

import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.client.shared.exception.ClientNotFoundException;
import com.os.workshop.features.client.shared.repository.ClientRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindMyOrdersHandlerTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ServiceOrderJpaRepository serviceOrderJpaRepository;

    @InjectMocks
    private FindMyOrdersHandler handler;

    private static final String VALID_CPF = "12345678909";
    private static final String EMAIL = "john@email.com";

    @Test
    void handle_whenClientExistsWithOrders_thenReturnsOrders() {
        Client client = Client.create("JOHN DOE", VALID_CPF, EMAIL, null);
        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));

        ServiceOrderEntity order = ServiceOrderEntity.builder()
                .id(UUID.randomUUID())
                .cpfCnpj(VALID_CPF)
                .placaVeiculo("ABC1234")
                .serviceStatus("RECEBIDA")
                .listService(List.of("TROCA_OLEO"))
                .build();
        when(serviceOrderJpaRepository.findByCpfCnpj(VALID_CPF)).thenReturn(List.of(order));

        List<ServiceOrderEntity> result = handler.handle(EMAIL);

        assertEquals(1, result.size());
        assertEquals("ABC1234", result.get(0).getPlacaVeiculo());
    }

    @Test
    void handle_whenClientExistsWithNoOrders_thenReturnsEmptyList() {
        Client client = Client.create("JOHN DOE", VALID_CPF, EMAIL, null);
        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(serviceOrderJpaRepository.findByCpfCnpj(VALID_CPF)).thenReturn(List.of());

        List<ServiceOrderEntity> result = handler.handle(EMAIL);

        assertTrue(result.isEmpty());
    }

    @Test
    void handle_whenClientNotFound_thenThrowsClientNotFoundException() {
        when(clientRepository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> handler.handle("unknown@email.com"));
    }
}

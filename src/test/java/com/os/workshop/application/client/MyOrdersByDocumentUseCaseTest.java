package com.os.workshop.application.client;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyOrdersByDocumentUseCaseTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CPF_FORMATTED = "529.982.247-25";

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @InjectMocks
    private FindMyOrdersUseCase findMyOrdersUseCase;

    private Client client() {
        return Client.reconstitute(1L, "JOHN DOE", VALID_CPF,
                "john@email.com", "11999999999", true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void executeByDocument_resolvesClientByCpf_andReturnsOrders() {
        ServiceOrder order = mock(ServiceOrder.class);
        when(clientRepository.findByDocument(VALID_CPF)).thenReturn(Optional.of(client()));
        when(serviceOrderRepository.findByCpfCnpj(VALID_CPF)).thenReturn(List.of(order));

        // Accepts a formatted CPF and normalizes it before lookup.
        List<ServiceOrder> result = findMyOrdersUseCase.executeByDocument(VALID_CPF_FORMATTED);

        assertEquals(1, result.size());
        verify(clientRepository).findByDocument(VALID_CPF);
        verify(serviceOrderRepository).findByCpfCnpj(VALID_CPF);
    }

    @Test
    void executeByDocument_whenClientMissing_throwsClientNotFound() {
        when(clientRepository.findByDocument(VALID_CPF)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class,
                () -> findMyOrdersUseCase.executeByDocument(VALID_CPF_FORMATTED));
    }
}

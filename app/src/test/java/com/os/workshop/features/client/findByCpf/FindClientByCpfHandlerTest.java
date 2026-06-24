package com.os.workshop.features.client.findByCpf;

import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.client.shared.exception.ClientNotFoundException;
import com.os.workshop.features.client.shared.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindClientByCpfHandlerTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private FindClientByCpfHandler handler;

    private static final String VALID_CPF = "12345678909";

    @Test
    void handle_whenClientExists_thenReturnsClient() {
        Client client = Client.create("JOHN DOE", VALID_CPF, "john@email.com", null);
        when(clientRepository.findByCpf(VALID_CPF)).thenReturn(Optional.of(client));

        Client result = handler.handle(VALID_CPF);

        assertNotNull(result);
        assertEquals("JOHN DOE", result.getName());
    }

    @Test
    void handle_whenClientNotFound_thenThrowsClientNotFoundException() {
        when(clientRepository.findByCpf(VALID_CPF)).thenReturn(Optional.empty());

        ClientNotFoundException ex = assertThrows(ClientNotFoundException.class,
                () -> handler.handle(VALID_CPF));

        assertTrue(ex.getMessage().contains(VALID_CPF));
    }

    @Test
    void handle_whenCpfHasFormatting_thenNormalizesBeforeQuery() {
        Client client = Client.create("JOHN DOE", VALID_CPF, null, null);
        when(clientRepository.findByCpf(VALID_CPF)).thenReturn(Optional.of(client));

        handler.handle("123.456.789-09");

        verify(clientRepository).findByCpf(VALID_CPF);
    }
}

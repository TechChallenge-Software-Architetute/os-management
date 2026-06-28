package com.os.workshop.features.client.findById;

import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.client.FindClientByIdUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindClientByIdUseCaseTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private FindClientByIdUseCase handler;

    @Test
    void handle_whenClientExists_thenReturnsClient() {
        Client client = Client.create("JOHN DOE", "12345678909", "john@email.com", "11999999999");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        Client result = handler.execute(1L);

        assertNotNull(result);
        assertEquals("JOHN DOE", result.getName());
    }

    @Test
    void handle_whenClientNotFound_thenThrowsClientNotFoundException() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        ClientNotFoundException ex = assertThrows(ClientNotFoundException.class, () -> handler.execute(99L));

        assertTrue(ex.getMessage().contains("99"));
    }
}

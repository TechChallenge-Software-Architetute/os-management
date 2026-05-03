package com.os.workshop.features.client.update;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateClientHandlerTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private UpdateClientHandler handler;

    private static final String VALID_CPF = "12345678909";

    @Test
    void handle_whenClientExists_thenUpdatesAndReturns() {
        Client client = Client.create("OLD NAME", VALID_CPF, "old@email.com", "11000000000");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new UpdateClientRequest("NEW NAME", VALID_CPF, "new@email.com", "11999999999");
        Client result = handler.handle(1L, request);

        assertEquals("NEW NAME", result.getName());
        assertEquals("new@email.com", result.getEmail());
        assertEquals("11999999999", result.getPhone());
        verify(clientRepository).save(client);
    }

    @Test
    void handle_whenClientNotFound_thenThrowsClientNotFoundException() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new UpdateClientRequest("NAME", VALID_CPF, null, null);
        assertThrows(ClientNotFoundException.class, () -> handler.handle(99L, request));

        verify(clientRepository, never()).save(any());
    }
}

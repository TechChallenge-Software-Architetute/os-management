package com.os.workshop.features.client.update;

import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.client.UpdateClientUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateClientUseCaseTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private UpdateClientUseCase handler;

    private static final String VALID_CPF = "12345678909";

    @Test
    void handle_whenClientExists_thenUpdatesAndReturns() {
        Client client = Client.create("OLD NAME", VALID_CPF, "old@email.com", "11000000000");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        Client result = handler.execute(1L, "NEW NAME", "new@email.com", "11999999999");

        assertEquals("NEW NAME", result.getName());
        assertEquals("new@email.com", result.getEmail());
        assertEquals("11999999999", result.getPhone());
        verify(clientRepository).save(client);
    }

    @Test
    void handle_whenClientNotFound_thenThrowsClientNotFoundException() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> handler.execute(99L, "NAME", null, null));

        verify(clientRepository, never()).save(any());
    }
}

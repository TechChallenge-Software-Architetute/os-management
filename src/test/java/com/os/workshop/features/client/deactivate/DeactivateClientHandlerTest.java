package com.os.workshop.features.client.deactivate;

import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.client.DeactivateClientUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeactivateClientUseCaseTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private DeactivateClientUseCase handler;

    @Test
    void handle_whenClientExists_thenDeactivatesAndSaves() {
        Client client = Client.create("JOHN DOE", "12345678909", null, null);
        assertTrue(client.isActive());
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        handler.execute(1L);

        assertFalse(client.isActive());
        verify(clientRepository).save(argThat(c -> !c.isActive()));
    }

    @Test
    void handle_whenClientNotFound_thenThrowsClientNotFoundException() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> handler.execute(99L));

        verify(clientRepository, never()).save(any());
    }
}

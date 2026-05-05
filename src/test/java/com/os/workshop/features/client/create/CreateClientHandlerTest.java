package com.os.workshop.features.client.create;

import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.client.shared.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateClientHandlerTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private CreateClientHandler handler;

    private static final String VALID_CPF = "12345678909";

    @Test
    void handle_whenCpfIsUnique_thenCreatesAndReturnsClient() {
        var request = new CreateClientRequest("JOHN DOE", VALID_CPF, "john@email.com", "11999999999");
        when(clientRepository.existsByCpf(VALID_CPF)).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        Client result = handler.handle(request);

        assertNotNull(result);
        assertEquals("JOHN DOE", result.getName());
        assertTrue(result.isActive());
        verify(clientRepository).existsByCpf(VALID_CPF);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void handle_whenCpfAlreadyExists_thenThrowsIllegalStateException() {
        var request = new CreateClientRequest("JOHN DOE", VALID_CPF, "john@email.com", "11999999999");
        when(clientRepository.existsByCpf(VALID_CPF)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> handler.handle(request));

        assertTrue(ex.getMessage().contains(VALID_CPF));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void handle_whenCpfHasFormatting_thenNormalizesBeforeCheck() {
        var request = new CreateClientRequest("JOHN DOE", "123.456.789-09", "john@email.com", null);
        when(clientRepository.existsByCpf(VALID_CPF)).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(request);

        verify(clientRepository).existsByCpf(VALID_CPF);
    }
}

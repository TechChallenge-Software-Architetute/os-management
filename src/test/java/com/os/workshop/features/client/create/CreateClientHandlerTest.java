package com.os.workshop.features.client.create;

import com.os.workshop.domain.client.Client;
import com.os.workshop.application.client.CreateClientUseCase;
import com.os.workshop.application.client.port.out.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateClientUseCaseTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private CreateClientUseCase handler;

    private static final String VALID_CPF = "12345678909";

    @Test
    void handle_whenCpfIsUnique_thenCreatesAndReturnsClient() {
        when(clientRepository.existsByCpf(VALID_CPF)).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        Client result = handler.execute("JOHN DOE", VALID_CPF, "john@email.com", "11999999999");

        assertNotNull(result);
        assertEquals("JOHN DOE", result.getName());
        assertTrue(result.isActive());
        verify(clientRepository).existsByCpf(VALID_CPF);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void handle_whenCpfAlreadyExists_thenThrowsIllegalStateException() {
        when(clientRepository.existsByCpf(VALID_CPF)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> handler.execute("JOHN DOE", VALID_CPF, "john@email.com", "11999999999"));

        assertTrue(ex.getMessage().contains(VALID_CPF));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void handle_whenCpfHasFormatting_thenNormalizesBeforeCheck() {
        when(clientRepository.existsByCpf(VALID_CPF)).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        handler.execute("JOHN DOE", "123.456.789-09", "john@email.com", null);

        verify(clientRepository).existsByCpf(VALID_CPF);
    }
}

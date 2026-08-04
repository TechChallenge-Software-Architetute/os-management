package com.os.workshop.application.client;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientUseCaseTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CPF_FORMATTED = "529.982.247-25";
    private static final String ANOTHER_CPF = "11144477735";
    private static final String ANOTHER_CPF_FORMATTED = "111.444.777-35";

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private CreateClientUseCase createClientUseCase;

    @InjectMocks
    private FindClientByIdUseCase findClientByIdUseCase;

    @InjectMocks
    private FindClientByCpfUseCase findClientByCpfUseCase;

    @InjectMocks
    private ListClientsUseCase listClientsUseCase;

    @InjectMocks
    private DeactivateClientUseCase deactivateClientUseCase;

    @InjectMocks
    private UpdateClientUseCase updateClientUseCase;

    private Client createClient() {
        return Client.reconstitute(1L, "JOHN DOE", VALID_CPF,
                "john@email.com", "11999999999", true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    // === CreateClientUseCase ===

    @Test
    void whenCreatingClientWithUniqueCpf_thenClientIsSaved() {
        when(clientRepository.existsByDocument(VALID_CPF)).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));

        Client result = createClientUseCase.execute("John Doe", VALID_CPF_FORMATTED, "john@email.com", "11999999999");

        assertEquals("JOHN DOE", result.getName());
        assertTrue(result.isActive());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void whenCreatingClientWithDuplicateCpf_thenThrowsIllegalState() {
        when(clientRepository.existsByDocument(VALID_CPF)).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                createClientUseCase.execute("John Doe", VALID_CPF_FORMATTED, "john@email.com", "11999999999"));
    }

    // === FindClientByIdUseCase ===

    @Test
    void whenFindingClientByExistingId_thenReturnsClient() {
        Client client = createClient();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        Client result = findClientByIdUseCase.execute(1L);

        assertEquals(1L, result.getId());
        assertEquals("JOHN DOE", result.getName());
    }

    @Test
    void whenFindingClientByNonExistingId_thenThrowsClientNotFound() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> findClientByIdUseCase.execute(999L));
    }

    // === FindClientByCpfUseCase ===

    @Test
    void whenFindingClientByExistingCpf_thenReturnsClient() {
        Client client = createClient();
        when(clientRepository.findByDocument(VALID_CPF)).thenReturn(Optional.of(client));

        Client result = findClientByCpfUseCase.execute(VALID_CPF_FORMATTED);

        assertEquals(VALID_CPF, result.getDocument().getValue());
    }

    @Test
    void whenFindingClientByNonExistingCpf_thenThrowsClientNotFound() {
        when(clientRepository.findByDocument(ANOTHER_CPF)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> findClientByCpfUseCase.execute(ANOTHER_CPF_FORMATTED));
    }

    // === ListClientsUseCase ===

    @Test
    void whenListingClients_thenReturnsActiveClients() {
        when(clientRepository.findAllActive()).thenReturn(List.of(createClient()));

        List<Client> result = listClientsUseCase.execute();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
    }

    @Test
    void whenListingClientsWithNoActiveClients_thenReturnsEmptyList() {
        when(clientRepository.findAllActive()).thenReturn(List.of());

        List<Client> result = listClientsUseCase.execute();

        assertTrue(result.isEmpty());
    }

    // === DeactivateClientUseCase ===

    @Test
    void whenDeactivatingActiveClient_thenClientIsSetInactive() {
        Client client = createClient();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));

        deactivateClientUseCase.execute(1L);

        assertFalse(client.isActive());
        verify(clientRepository).save(client);
    }

    @Test
    void whenDeactivatingNonExistingClient_thenThrowsClientNotFound() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> deactivateClientUseCase.execute(999L));
    }

    // === UpdateClientUseCase ===

    @Test
    void whenUpdatingExistingClient_thenClientIsUpdatedAndSaved() {
        Client client = createClient();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));

        Client result = updateClientUseCase.execute(1L, "Jane Doe", "jane@email.com", "11888888888");

        assertEquals("Jane Doe", result.getName());
        assertEquals("jane@email.com", result.getEmail());
        assertEquals("11888888888", result.getPhone());
        verify(clientRepository).save(client);
    }

    @Test
    void whenUpdatingNonExistingClient_thenThrowsClientNotFound() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () ->
                updateClientUseCase.execute(999L, "Jane Doe", "jane@email.com", "11888888888"));
    }
}

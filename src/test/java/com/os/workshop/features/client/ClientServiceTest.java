package com.os.workshop.features.client;

import com.os.workshop.features.client.domain.Client;
import com.os.workshop.features.client.dto.ClientRequest;
import com.os.workshop.features.client.exception.ClientNotFoundException;
import com.os.workshop.features.client.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CPF_FORMATTED = "529.982.247-25";

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private ClientRequest createRequest() {
        return new ClientRequest("João Silva", VALID_CPF, "joao@email.com", "11999998888");
    }

    private Client createClient() {
        return Client.reconstitute(
                1L, "JOÃO SILVA", VALID_CPF, "joao@email.com", "11999998888",
                true, null, null);
    }

    @Test
    void whenCreatingClientWithUniqueCpf_thenClientIsSaved() {
        when(clientRepository.existsByCpf(VALID_CPF)).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));

        Client result = clientService.create(createRequest());

        assertNotNull(result);
        assertEquals(VALID_CPF, result.getCpf().getValue());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void whenCreatingClientWithDuplicateCpf_thenThrowsIllegalState() {
        when(clientRepository.existsByCpf(VALID_CPF)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> clientService.create(createRequest()));
    }

    @Test
    void whenFindingClientByExistingId_thenReturnsClient() {
        Client client = createClient();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        Client result = clientService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("JOÃO SILVA", result.getName());
    }

    @Test
    void whenFindingClientByNonExistingId_thenThrowsClientNotFound() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> clientService.findById(999L));
    }

    @Test
    void whenFindingClientByExistingCpf_thenReturnsClient() {
        Client client = createClient();
        when(clientRepository.findByCpf(VALID_CPF)).thenReturn(Optional.of(client));

        Client result = clientService.findByCpf(VALID_CPF);

        assertEquals(VALID_CPF, result.getCpf().getValue());
    }

    @Test
    void whenFindingClientByFormattedCpf_thenNormalizesAndReturnsClient() {
        Client client = createClient();
        when(clientRepository.findByCpf(VALID_CPF)).thenReturn(Optional.of(client));

        Client result = clientService.findByCpf(VALID_CPF_FORMATTED);

        assertEquals(VALID_CPF, result.getCpf().getValue());
    }

    @Test
    void whenFindingClientByNonExistingCpf_thenThrowsClientNotFound() {
        when(clientRepository.findByCpf(VALID_CPF)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> clientService.findByCpf(VALID_CPF));
    }

    @Test
    void whenFindingAllClients_thenReturnsActiveClients() {
        when(clientRepository.findAllActive()).thenReturn(List.of(createClient()));

        List<Client> result = clientService.findAll();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
    }

    @Test
    void whenUpdatingExistingClient_thenClientDataIsChanged() {
        Client client = createClient();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));

        ClientRequest updateRequest = new ClientRequest("Maria Souza", VALID_CPF, "maria@email.com", "11888887777");
        Client result = clientService.update(1L, updateRequest);

        assertEquals("Maria Souza", result.getName());
        assertEquals("maria@email.com", result.getEmail());
        assertEquals("11888887777", result.getPhone());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void whenUpdatingNonExistingClient_thenThrowsClientNotFound() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class,
                () -> clientService.update(999L, createRequest()));
    }

    @Test
    void whenDeactivatingExistingClient_thenClientIsSetInactive() {
        Client client = createClient();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));

        clientService.deactivate(1L);

        assertFalse(client.isActive());
        verify(clientRepository).save(client);
    }

    @Test
    void whenDeactivatingNonExistingClient_thenThrowsClientNotFound() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> clientService.deactivate(999L));
    }
}

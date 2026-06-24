package com.os.workshop.features.client.list;

import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.client.shared.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListClientsHandlerTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ListClientsHandler handler;

    @Test
    void handle_whenClientsExist_thenReturnsList() {
        Client c1 = Client.create("ALICE", "12345678909", null, null);
        Client c2 = Client.create("BOB", "98765432100", null, null);
        when(clientRepository.findAllActive()).thenReturn(List.of(c1, c2));

        List<Client> result = handler.handle();

        assertEquals(2, result.size());
    }

    @Test
    void handle_whenNoClients_thenReturnsEmptyList() {
        when(clientRepository.findAllActive()).thenReturn(List.of());

        List<Client> result = handler.handle();

        assertTrue(result.isEmpty());
    }
}

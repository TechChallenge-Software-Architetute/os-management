package com.os.workshop.features.client.list;

import com.os.workshop.domain.client.Client;
import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.client.ListClientsUseCase;
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
class ListClientsUseCaseTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ListClientsUseCase handler;

    @Test
    void handle_whenClientsExist_thenReturnsList() {
        Client c1 = Client.create("ALICE", "12345678909", null, null);
        Client c2 = Client.create("BOB", "98765432100", null, null);
        when(clientRepository.findAllActive()).thenReturn(List.of(c1, c2));

        List<Client> result = handler.execute();

        assertEquals(2, result.size());
    }

    @Test
    void handle_whenNoClients_thenReturnsEmptyList() {
        when(clientRepository.findAllActive()).thenReturn(List.of());

        List<Client> result = handler.execute();

        assertTrue(result.isEmpty());
    }
}

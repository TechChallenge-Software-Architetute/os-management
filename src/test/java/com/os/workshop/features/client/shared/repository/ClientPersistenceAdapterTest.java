package com.os.workshop.features.client.shared.repository;

import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.client.shared.mapper.ClientMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientPersistenceAdapterTest {

    @Mock
    private ClientJpaRepository jpaRepository;

    @Mock
    private ClientMapper mapper;

    @InjectMocks
    private ClientPersistenceAdapter adapter;

    @Test
    void savesExistingClientAndQueriesRepository() {
        Client client = Client.reconstitute(1L, "ANA", "52998224725", "a@b.com", "999", true, null, null);
        ClientEntity entity = new ClientEntity();

        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(client);
        when(jpaRepository.findByCpf("52998224725")).thenReturn(Optional.of(entity));
        when(jpaRepository.findByActiveTrue()).thenReturn(List.of(entity));
        when(jpaRepository.existsByCpf("52998224725")).thenReturn(true);

        assertEquals(1L, adapter.save(client).getId());
        assertTrue(adapter.findById(1L).isPresent());
        assertTrue(adapter.findByCpf("52998224725").isPresent());
        assertEquals(1, adapter.findAllActive().size());
        assertTrue(adapter.existsByCpf("52998224725"));
        verify(mapper).updateEntity(entity, client);
    }
}

package com.os.workshop.features.client.persistence.mapper;

import com.os.workshop.features.client.domain.Client;
import com.os.workshop.features.client.persistence.entity.ClientEntity;
import com.os.workshop.features.client.persistence.mapper.ClientMapper;
import com.os.workshop.features.client.persistence.mapper.ClientMapperImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientMapperTest {

    private final ClientMapper mapper = new ClientMapperImpl();

    @Test
    void convertsDomainAndEntity() {
        Client client = Client.reconstitute(1L, "ANA", "52998224725", "a@b.com", "999", true, LocalDateTime.now(), LocalDateTime.now());
        ClientEntity entity = mapper.toEntity(client);
        entity.setId(1L);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        Client domain = mapper.toDomain(entity);

        assertEquals("52998224725", entity.getCpf());
        assertEquals("52998224725", domain.getCpf().getValue());
    }
}

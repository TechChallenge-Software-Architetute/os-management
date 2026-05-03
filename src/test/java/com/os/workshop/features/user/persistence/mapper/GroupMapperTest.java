package com.os.workshop.features.user.persistence.mapper;

import com.os.workshop.features.user.domain.Group;
import com.os.workshop.features.user.persistence.entity.GroupEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GroupMapperTest {

    private final GroupMapper mapper = Mappers.getMapper(GroupMapper.class);

    @Test
    void shouldMapEntityToDomain() {
        // Arrange
        GroupEntity entity = new GroupEntity();
        UUID uuid = UUID.randomUUID();
        entity.setId(uuid);
        entity.setName("ADMIN");

        Group domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(entity.getName(), domain.getName());
    }

    @Test
    void shouldMapDomainToEntity() {
        Group domain = new Group();
        domain.setName("USER");

        GroupEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(domain.getName(), entity.getName());
    }

    @Test
    void shouldHandleNullEntity() {
        Group result = mapper.toDomain(null);
        assertNull(result);
    }

    @Test
    void shouldHandleNullDomain() {
        GroupEntity result = mapper.toEntity(null);
        assertNull(result);
    }
}
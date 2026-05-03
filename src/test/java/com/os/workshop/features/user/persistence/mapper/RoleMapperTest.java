package com.os.workshop.features.user.persistence.mapper;

import com.os.workshop.features.user.domain.Role;
import com.os.workshop.features.user.persistence.entity.RoleEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoleMapperTest {

    private final RoleMapper mapper = Mappers.getMapper(RoleMapper.class);

    @Test
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();

        RoleEntity entity = new RoleEntity();
        entity.setId(id);
        entity.setName("ADMIN");

        Role domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(entity.getName(), domain.getName());
    }

    @Test
    void shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();

        Role role = new Role();
        role.setName("USER");

        RoleEntity entity = mapper.toEntity(role);

        assertNotNull(entity);
        assertEquals(role.getName(), entity.getName());
    }

    @Test
    void shouldHandleNullEntity() {
        Role result = mapper.toDomain(null);
        assertNull(result);
    }

    @Test
    void shouldHandleNullDomain() {
        RoleEntity result = mapper.toEntity(null);
        assertNull(result);
    }
}
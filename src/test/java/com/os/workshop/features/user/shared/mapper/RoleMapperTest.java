package com.os.workshop.features.user.shared.mapper;

import com.os.workshop.features.user.shared.domain.Role;
import com.os.workshop.features.user.shared.repository.RoleEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoleMapperTest {

    private final RoleMapper mapper = Mappers.getMapper(RoleMapper.class);

    @Test
    void shouldMapEntityToDomain() {
        RoleEntity entity = new RoleEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("ADMIN");

        assertEquals("ADMIN", mapper.toDomain(entity).getName());
    }

    @Test
    void shouldMapDomainToEntity() {
        Role role = new Role();
        role.setName("USER");

        assertEquals("USER", mapper.toEntity(role).getName());
    }

    @Test
    void shouldHandleNullEntity() { assertNull(mapper.toDomain(null)); }

    @Test
    void shouldHandleNullDomain() { assertNull(mapper.toEntity(null)); }
}

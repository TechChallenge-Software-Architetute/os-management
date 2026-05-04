package com.os.workshop.features.user.shared.mapper;

import com.os.workshop.features.user.shared.domain.Group;
import com.os.workshop.features.user.shared.repository.GroupEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GroupMapperTest {

    private final GroupMapper mapper = Mappers.getMapper(GroupMapper.class);

    @Test
    void shouldMapEntityToDomain() {
        GroupEntity entity = new GroupEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("ADMIN");

        assertEquals("ADMIN", mapper.toDomain(entity).getName());
    }

    @Test
    void shouldMapDomainToEntity() {
        Group domain = new Group();
        domain.setName("USER");

        assertEquals("USER", mapper.toEntity(domain).getName());
    }

    @Test
    void shouldHandleNullEntity() { assertNull(mapper.toDomain(null)); }

    @Test
    void shouldHandleNullDomain() { assertNull(mapper.toEntity(null)); }
}

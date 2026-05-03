package com.os.workshop.features.user.shared.repository;

import com.os.workshop.features.user.shared.domain.Role;
import com.os.workshop.features.user.shared.mapper.RoleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolePersistenceAdapterTest {

    @Mock private JpaRoleRepository jpaRoleRepository;
    @Mock private RoleMapper roleMapper;
    @InjectMocks private RolePersistenceAdapter adapter;

    @Test
    void shouldReturnRoleWhenFound() {
        RoleEntity entity = new RoleEntity();
        entity.setName("ADMIN");
        Role domain = new Role();
        domain.setName("ADMIN");

        when(jpaRoleRepository.findByName("ADMIN")).thenReturn(entity);
        when(roleMapper.toDomain(entity)).thenReturn(domain);

        assertEquals("ADMIN", adapter.findByRoleName("ADMIN").getName());
    }

    @Test
    void shouldReturnNullWhenNotFound() {
        when(jpaRoleRepository.findByName("NOT_FOUND")).thenReturn(null);
        when(roleMapper.toDomain(null)).thenReturn(null);

        assertNull(adapter.findByRoleName("NOT_FOUND"));
    }
}

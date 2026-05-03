package com.os.workshop.features.user.persistence.adapter;

import com.os.workshop.features.user.domain.Role;
import com.os.workshop.features.user.persistence.entity.RoleEntity;
import com.os.workshop.features.user.persistence.mapper.RoleMapper;
import com.os.workshop.features.user.persistence.repository.JpaRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleRepositoryAdapterTest {

    @Mock
    private JpaRoleRepository jpaRoleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleRepositoryAdapter adapter;

    @Test
    void shouldReturnRoleWhenFound() {
        // Arrange
        String roleName = "ADMIN";

        RoleEntity entity = new RoleEntity();
        entity.setName(roleName);

        Role domain = new Role();
        domain.setName(roleName);

        when(jpaRoleRepository.findByName(roleName)).thenReturn(entity);
        when(roleMapper.toDomain(entity)).thenReturn(domain);

        Role result = adapter.findByRoleName(roleName);

        assertNotNull(result);
        assertEquals(roleName, result.getName());

        verify(jpaRoleRepository).findByName(roleName);
        verify(roleMapper).toDomain(entity);
    }

    @Test
    void shouldReturnNullWhenRepositoryReturnsNull() {
        String roleName = "NOT_FOUND";

        when(jpaRoleRepository.findByName(roleName)).thenReturn(null);
        when(roleMapper.toDomain(null)).thenReturn(null);

        Role result = adapter.findByRoleName(roleName);

        assertNull(result);

        verify(jpaRoleRepository).findByName(roleName);
        verify(roleMapper).toDomain(null);
    }
}
package com.os.features.user.persistence.adapter;

import com.os.features.user.domain.User;
import com.os.features.user.dto.SignUpRequest;
import com.os.features.user.dto.SignUpResponse;
import com.os.features.user.persistence.entity.RoleEntity;
import com.os.features.user.persistence.entity.UserEntity;
import com.os.features.user.persistence.mapper.UserMapper;
import com.os.features.user.persistence.repository.JpaRoleRepository;
import com.os.features.user.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private JpaUserRepository jpaUserRepository;

    @Mock
    private JpaRoleRepository jpaRoleRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    @Test
    void shouldReturnUserWhenEmailExists() {
        String email = "test@email.com";

        UserEntity entity = new UserEntity();
        entity.setEmail(email);

        User domain = new User();
        domain.setEmail(email);

        when(jpaUserRepository.findByEmail(email)).thenReturn(Optional.of(entity));
        when(userMapper.userEntityToUser(entity)).thenReturn(domain);

        Optional<User> result = adapter.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());

        verify(jpaUserRepository).findByEmail(email);
        verify(userMapper).userEntityToUser(entity);
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        String email = "notfound@email.com";

        when(jpaUserRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<User> result = adapter.findByEmail(email);

        assertTrue(result.isEmpty());
        verify(jpaUserRepository).findByEmail(email);
        verifyNoInteractions(userMapper);
    }

    @Test
    void shouldSaveUserWithNormalizedRoles() {
        String email = "user@email.com";
        String password = "encrypted";

        SignUpRequest request = new SignUpRequest(
                email,
                "123",
                Set.of("admin", "ROLE_user")
        );

        RoleEntity adminRole = new RoleEntity();
        adminRole.setName("ROLE_ADMIN");

        RoleEntity userRole = new RoleEntity();
        userRole.setName("ROLE_USER");

        when(jpaRoleRepository.findByName("ROLE_ADMIN")).thenReturn(adminRole);
        when(jpaRoleRepository.findByName("ROLE_USER")).thenReturn(userRole);

        UserEntity savedEntity = new UserEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setEmail(email);
        savedEntity.setRoles(Set.of(adminRole, userRole));

        when(jpaUserRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        SignUpResponse response = adapter.save(request, password);

        assertNotNull(response);
        assertEquals(email, response.email());
        assertEquals(2, response.roles().size());
        assertTrue(response.roles().contains("ROLE_ADMIN"));
        assertTrue(response.roles().contains("ROLE_USER"));

        verify(jpaRoleRepository).findByName("ROLE_ADMIN");
        verify(jpaRoleRepository).findByName("ROLE_USER");
        verify(jpaUserRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldSaveUserWithoutRolesWhenNull() {
        String email = "user@email.com";

        SignUpRequest request = new SignUpRequest(
                email,
                "123",
                null
        );

        UserEntity savedEntity = new UserEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setEmail(email);
        savedEntity.setRoles(Set.of());

        when(jpaUserRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        SignUpResponse response = adapter.save(request, "encrypted");

        assertNotNull(response);
        assertTrue(response.roles().isEmpty());

        verifyNoInteractions(jpaRoleRepository);
        verify(jpaUserRepository).save(any(UserEntity.class));
    }
}
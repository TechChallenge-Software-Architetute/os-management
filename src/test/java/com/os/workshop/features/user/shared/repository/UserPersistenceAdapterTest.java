package com.os.workshop.features.user.shared.repository;

import com.os.workshop.features.user.shared.domain.User;
import com.os.workshop.features.user.shared.mapper.UserMapper;
import com.os.workshop.features.user.signUp.SignUpRequest;
import com.os.workshop.features.user.signUp.SignUpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {

    @Mock
    private JpaUserRepository jpaUserRepository;

    @Mock
    private JpaRoleRepository jpaRoleRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserPersistenceAdapter adapter;

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
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        when(jpaUserRepository.findByEmail("notfound@email.com")).thenReturn(Optional.empty());

        assertTrue(adapter.findByEmail("notfound@email.com").isEmpty());
    }

    @Test
    void shouldSaveUserWithNormalizedRoles() {
        SignUpRequest request = new SignUpRequest("user@email.com", "123", Set.of("admin", "ROLE_user"));

        RoleEntity adminRole = new RoleEntity();
        adminRole.setName("ROLE_ADMIN");
        RoleEntity userRole = new RoleEntity();
        userRole.setName("ROLE_USER");

        when(jpaRoleRepository.findByName("ROLE_ADMIN")).thenReturn(adminRole);
        when(jpaRoleRepository.findByName("ROLE_USER")).thenReturn(userRole);

        UserEntity savedEntity = new UserEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setEmail("user@email.com");
        savedEntity.setRoles(Set.of(adminRole, userRole));

        when(jpaUserRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        SignUpResponse response = adapter.save(request, "encrypted");

        assertNotNull(response);
        assertEquals(2, response.roles().size());
    }

    @Test
    void shouldSaveUserWithoutRolesWhenNull() {
        SignUpRequest request = new SignUpRequest("user@email.com", "123", null);

        UserEntity savedEntity = new UserEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setEmail("user@email.com");
        savedEntity.setRoles(Set.of());

        when(jpaUserRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        SignUpResponse response = adapter.save(request, "encrypted");

        assertTrue(response.roles().isEmpty());
    }
}

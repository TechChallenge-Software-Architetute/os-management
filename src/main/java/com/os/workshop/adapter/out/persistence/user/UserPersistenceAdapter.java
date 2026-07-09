package com.os.workshop.adapter.out.persistence.user;

import com.os.workshop.application.user.port.out.UserRepository;
import com.os.workshop.domain.user.User;
import com.os.workshop.infrastructure.persistence.user.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final JpaRoleRepository jpaRoleRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public SignUpResult save(String email, String encryptedPassword, Set<String> roles) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(encryptedPassword);

        Set<RoleEntity> roleEntities = roles != null
                ? roles.stream().map(r -> jpaRoleRepository.findByName(normalizeRole(r))).collect(Collectors.toSet())
                : Set.of();
        user.setRoles(roleEntities);

        UserEntity saved = jpaUserRepository.save(user);
        return new SignUpResult(saved.getId(), saved.getEmail(),
                saved.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toSet()));
    }

    private static @NonNull String normalizeRole(String roleName) {
        return roleName.contains("ROLE") ? roleName.toUpperCase() : "ROLE_" + roleName.toUpperCase();
    }
}

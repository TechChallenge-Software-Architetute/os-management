package com.os.workshop.user.persistence.adapter;

import com.os.workshop.user.domain.User;
import com.os.workshop.user.dto.SignUpRequest;
import com.os.workshop.user.dto.SignUpResponse;
import com.os.workshop.user.persistence.entity.RoleEntity;
import com.os.workshop.user.persistence.entity.UserEntity;
import com.os.workshop.user.persistence.mapper.UserMapper;
import com.os.workshop.user.persistence.repository.JpaRoleRepository;
import com.os.workshop.user.persistence.repository.JpaUserRepository;
import com.os.workshop.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserMapper userMapper;
    private final JpaRoleRepository jpaRoleRepository;

    public UserRepositoryAdapter(JpaUserRepository jpaUserRepository, UserMapper userMapper, JpaRoleRepository jpaRoleRepository) {
        this.jpaUserRepository = jpaUserRepository;
        this.userMapper = userMapper;
        this.jpaRoleRepository = jpaRoleRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email {}", email);
        return jpaUserRepository.findByEmail(email)
                .map(userMapper::userEntityToUser);
    }

    @Override
    public SignUpResponse save(SignUpRequest request, String encryptedPassword) {
        log.debug("Saving a new user into database: {}", request.email());
        UserEntity user = new UserEntity();
        user.setEmail(request.email());
        user.setPassword(encryptedPassword);

        Set<RoleEntity> roles = request.roles() != null
                ? request.roles().stream()
                .map(roleName -> jpaRoleRepository.findByName(normalizeRole(roleName)))
                .collect(Collectors.toSet())
                : Set.of();

        user.setRoles(roles);

        UserEntity saved = jpaUserRepository.save(user);

        return new SignUpResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getRoles()
                        .stream()
                        .map(RoleEntity::getName)
                        .collect(Collectors.toSet())
        );
    }

    private static @NonNull String normalizeRole(String roleName) {
        return roleName.contains("ROLE") ?
                roleName.toUpperCase() : "ROLE_" + roleName.toUpperCase();
    }
}
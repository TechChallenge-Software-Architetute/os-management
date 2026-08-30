package com.os.workshop.application.user.port.out;

import com.os.workshop.domain.user.User;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findByCpf(String cpf);
    SignUpResult save(String email, String cpf, String encryptedPassword, Set<String> roles);

    record SignUpResult(UUID id, String email, String cpf, Set<String> roles) {}
}

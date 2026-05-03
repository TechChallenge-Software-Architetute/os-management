package com.os.workshop.features.user.repository;

import com.os.workshop.features.user.domain.User;
import com.os.workshop.features.user.dto.SignUpRequest;
import com.os.workshop.features.user.dto.SignUpResponse;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    SignUpResponse save(SignUpRequest user, String encryptedPassword);
}

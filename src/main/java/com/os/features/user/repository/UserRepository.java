package com.os.features.user.repository;

import com.os.features.user.domain.User;
import com.os.features.user.dto.SignUpRequest;
import com.os.features.user.dto.SignUpResponse;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    SignUpResponse save(SignUpRequest user, String encryptedPassword);
}

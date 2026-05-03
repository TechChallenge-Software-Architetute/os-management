package com.os.workshop.features.user.shared.repository;

import com.os.workshop.features.user.shared.domain.User;
import com.os.workshop.features.user.signUp.SignUpRequest;
import com.os.workshop.features.user.signUp.SignUpResponse;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    SignUpResponse save(SignUpRequest user, String encryptedPassword);
}

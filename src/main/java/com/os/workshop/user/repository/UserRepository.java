package com.os.workshop.user.repository;

import com.os.workshop.user.domain.User;
import com.os.workshop.user.dto.SignUpRequest;
import com.os.workshop.user.dto.SignUpResponse;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    SignUpResponse save(SignUpRequest user, String encryptedPassword);
}

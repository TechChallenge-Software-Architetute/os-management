package com.os.workshop.features.user.repository;

import com.os.workshop.features.user.domain.User;

public interface AuthRepository {
    User getUserByEmail(String email);
}

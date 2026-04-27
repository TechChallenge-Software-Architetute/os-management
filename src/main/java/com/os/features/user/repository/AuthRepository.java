package com.os.features.user.repository;

import com.os.features.user.domain.User;

public interface AuthRepository {
    User getUserByEmail(String email);
}

package com.os.workshop.user.repository;

import com.os.workshop.user.domain.User;

public interface AuthRepository {
    User getUserByEmail(String email);
}

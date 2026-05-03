package com.os.workshop.features.user.auth.iterator;

import com.os.workshop.features.user.domain.User;
import com.os.workshop.features.user.repository.AuthRepository;
import com.os.workshop.features.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthIterator implements AuthRepository {

    private final UserRepository userRepository;

    public AuthIterator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getUserByEmail(String email) {
        var user = userRepository.findByEmail(email).orElseThrow();
        user.setPassword(null);

        return user;
    }

}

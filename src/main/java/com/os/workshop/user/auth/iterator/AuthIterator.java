package com.os.workshop.user.auth.iterator;

import com.os.workshop.user.domain.User;
import com.os.workshop.user.repository.AuthRepository;
import com.os.workshop.user.repository.UserRepository;
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

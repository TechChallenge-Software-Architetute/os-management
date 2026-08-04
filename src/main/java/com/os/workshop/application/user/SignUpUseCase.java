package com.os.workshop.application.user;

import com.os.workshop.application.user.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class SignUpUseCase {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserRepository.SignUpResult execute(String email, String password, Set<String> roles) {
        return userRepository.save(email, passwordEncoder.encode(password), roles);
    }
}

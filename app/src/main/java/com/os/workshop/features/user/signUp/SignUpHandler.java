package com.os.workshop.features.user.signUp;

import com.os.workshop.features.user.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignUpHandler {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public SignUpResponse handle(SignUpRequest request) {
        return userRepository.save(request, passwordEncoder.encode(request.password()));
    }
}

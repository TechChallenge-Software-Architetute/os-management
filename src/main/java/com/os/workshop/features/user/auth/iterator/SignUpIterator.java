package com.os.workshop.features.user.auth.iterator;

import com.os.workshop.features.user.dto.SignUpRequest;
import com.os.workshop.features.user.dto.SignUpResponse;
import com.os.workshop.features.user.repository.SignUpRepository;
import com.os.workshop.features.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignUpIterator implements SignUpRepository {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;


    public SignUpIterator(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public SignUpResponse signUp(SignUpRequest signUpRequest) {
        return userRepository.save(signUpRequest, passwordEncoder.encode(signUpRequest.password()));
    }
}

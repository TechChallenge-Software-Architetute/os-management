package com.os.features.user.auth.iterator;

import com.os.features.user.dto.SignUpRequest;
import com.os.features.user.dto.SignUpResponse;
import com.os.features.user.repository.SignUpRepository;
import com.os.features.user.repository.UserRepository;
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

package com.os.workshop.application.user;

import com.os.workshop.application.user.port.out.UserRepository;
import com.os.workshop.domain.shared.Cpf;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class SignUpUseCase {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserRepository.SignUpResult execute(String email, String cpf, String password, Set<String> roles) {
        String normalizedCpf = new Cpf(cpf).getValue();
        return userRepository.save(email, normalizedCpf, passwordEncoder.encode(password), roles);
    }
}

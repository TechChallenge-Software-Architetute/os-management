package com.os.workshop.application.user;

import com.os.workshop.application.user.port.out.UserRepository;
import com.os.workshop.domain.user.User;
import com.os.workshop.infrastructure.security.JwtProperties;
import com.os.workshop.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    public LoginResult execute(String email, String password) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        User user = userRepository.findByEmail(email).orElseThrow();
        user.setPassword(null);

        String token = jwtService.generateToken(user);
        return new LoginResult(token, "Bearer", jwtProperties.getExpiration());
    }

    public record LoginResult(String token, String type, long expiresIn) {}
}

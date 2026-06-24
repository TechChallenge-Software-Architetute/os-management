package com.os.workshop.features.user.login;

import com.os.workshop.features.user.shared.domain.User;
import com.os.workshop.features.user.shared.repository.UserRepository;
import com.os.workshop.features.user.shared.security.JwtProperties;
import com.os.workshop.features.user.shared.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginHandler {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    public LoginResponse handle(LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email()).orElseThrow();
        user.setPassword(null);

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                "Bearer",
                jwtProperties.getExpiration()
        );
    }
}

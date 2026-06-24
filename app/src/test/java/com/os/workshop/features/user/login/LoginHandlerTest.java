package com.os.workshop.features.user.login;

import com.os.workshop.features.user.shared.domain.Role;
import com.os.workshop.features.user.shared.domain.User;
import com.os.workshop.features.user.shared.repository.UserRepository;
import com.os.workshop.features.user.shared.security.JwtProperties;
import com.os.workshop.features.user.shared.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoginHandlerTest {

    private final AuthenticationManager authManager = mock(AuthenticationManager.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final JwtProperties jwtProperties = new JwtProperties("secret", 3600L);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final LoginHandler handler = new LoginHandler(authManager, jwtService, jwtProperties, userRepository);

    @Test
    void authenticatesAndReturnsToken() {
        LoginRequest request = new LoginRequest("a@b.com", "raw");
        User user = new User(UUID.randomUUID(), "a@b.com", "pwd", Set.of(new Role("ADMIN")), Set.of());

        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("token");

        LoginResponse response = handler.handle(request);

        assertNotNull(response);
        assertEquals("token", response.token());
        assertEquals(3600L, response.expiresIn());
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}

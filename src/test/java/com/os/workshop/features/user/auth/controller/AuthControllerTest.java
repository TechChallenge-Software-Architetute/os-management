package com.os.workshop.features.user.auth.controller;

import com.os.workshop.features.user.auth.controller.AuthController;
import com.os.workshop.features.user.auth.iterator.AuthIterator;
import com.os.workshop.features.user.domain.Role;
import com.os.workshop.features.user.domain.User;
import com.os.workshop.features.user.dto.LoginRequest;
import com.os.workshop.features.user.security.config.JwtProperties;
import com.os.workshop.features.user.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthIterator authIterator = mock(AuthIterator.class);
    private final AuthController controller = new AuthController(
            authenticationManager,
            jwtService,
            new JwtProperties("secret", 3600L),
            authIterator
    );

    @Test
    void authenticatesAndReturnsToken() {
        LoginRequest request = new LoginRequest("a@b.com", "raw");
        User user = new User(UUID.randomUUID(), "a@b.com", "pwd", Set.of(new Role("ADMIN")), Set.of());

        when(authIterator.getUserByEmail("a@b.com")).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("token");

        var response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token", response.getBody().token());
        assertEquals(3600L, response.getBody().expiresIn());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}

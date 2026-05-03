package com.os.workshop.features.user.shared.security;

import com.os.workshop.features.user.shared.domain.Role;
import com.os.workshop.features.user.shared.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties("my-super-secret-key-that-is-long-enough-123456", 1000 * 60 * 60);
        jwtService = new JwtService(props);
    }

    @Test
    void shouldGenerateValidToken() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@email.com");
        user.setRoles(Set.of(new Role("ROLE_USER")));

        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertEquals(user.getEmail(), jwtService.extractUsername(token));
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("valid@email.com");
        user.setRoles(Set.of());

        String token = jwtService.generateToken(user);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("valid@email.com");

        assertTrue(jwtService.isValid(token, userDetails));
    }

    @Test
    void shouldInvalidateTokenWithDifferentUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("real@email.com");
        user.setRoles(Set.of());

        String token = jwtService.generateToken(user);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("fake@email.com");

        assertFalse(jwtService.isValid(token, userDetails));
    }

    @Test
    void shouldDetectExpiredToken() {
        JwtService shortJwtService = new JwtService(
                new JwtProperties("my-super-secret-key-that-is-long-enough-123456", 1));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("expire@email.com");
        user.setRoles(Set.of());

        String token = shortJwtService.generateToken(user);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("expire@email.com");

        assertFalse(shortJwtService.isValid(token, userDetails));
    }

    @Test
    void shouldContainCorrectClaims() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("claims@email.com");
        user.setRoles(Set.of(new Role("ROLE_ADMIN")));

        String token = jwtService.generateToken(user);
        assertEquals("claims@email.com", jwtService.extractUsername(token));
    }
}

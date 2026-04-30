package com.os.features.user.security.jwt;

import com.os.features.user.domain.Role;
import com.os.features.user.domain.User;
import com.os.features.user.security.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        Role role = new Role();
        role.setName("ROLE_USER");

        user.setRoles(Set.of(role));

        String token = jwtService.generateToken(user);

        assertNotNull(token);

        String username = jwtService.extractUsername(token);
        assertEquals(user.getEmail(), username);
    }

    @Test
    void shouldContainCorrectClaims() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("claims@email.com");

        Role role = new Role();
        role.setName("ROLE_ADMIN");

        user.setRoles(Set.of(role));

        String token = jwtService.generateToken(user);

        String extractedUsername = jwtService.extractUsername(token);

        assertEquals("claims@email.com", extractedUsername);
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

        boolean isValid = jwtService.isValid(token, userDetails);

        assertTrue(isValid);
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

        boolean isValid = jwtService.isValid(token, userDetails);

        assertFalse(isValid);
    }

    @Test
    void shouldDetectExpiredToken() throws InterruptedException {
        JwtProperties shortProps = new JwtProperties("my-super-secret-key-that-is-long-enough-123456", 1);

        JwtService shortJwtService = new JwtService(shortProps);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("expire@email.com");
        user.setRoles(Set.of());

        String token = shortJwtService.generateToken(user);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("expire@email.com");

        boolean isValid = shortJwtService.isValid(token, userDetails);

        assertFalse(isValid);
    }
}
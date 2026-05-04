package com.os.workshop.features.user.shared.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void providesPasswordEncoder() {
        SecurityConfig config = new SecurityConfig(mock(JwtFilter.class));

        assertNotNull(config.passwordEncoder().encode("secret"));
    }
}

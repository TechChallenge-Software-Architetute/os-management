package com.os.workshop.user.security.config;

import com.os.workshop.features.user.security.config.SecurityConfig;
import com.os.workshop.features.user.security.filter.JwtFilter;
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

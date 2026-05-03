package com.os.workshop.user.security.config;

import com.os.workshop.features.user.security.config.JwtProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtPropertiesTest {

    @Test
    void exposesConfiguredValues() {
        JwtProperties properties = new JwtProperties("secret", 1000L);

        assertEquals("secret", properties.getSecret());
        assertEquals(1000L, properties.getExpiration());
    }
}

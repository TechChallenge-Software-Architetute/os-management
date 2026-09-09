package com.os.workshop.infrastructure.security;

import com.os.workshop.domain.user.Role;
import com.os.workshop.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-that-is-long-enough-for-hs256-signing!!";
    private final JwtService jwtService = new JwtService(new JwtProperties(SECRET, 3_600_000L));
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private String clientToken(long expiresInMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject("52998224725")
                .claim("clientId", 1)
                .claim("name", "JOAO DA SILVA")
                .claim("roles", java.util.List.of("CLIENT"))
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiresInMs))
                .signWith(key)
                .compact();
    }

    @Test
    void staffTokenRoundTrips() {
        User user = new User(UUID.randomUUID(), "admin@workshop.com", "hash",
                Set.of(new Role("ROLE_ADMIN")), Set.of());

        Claims claims = jwtService.parseClaims(jwtService.generateToken(user));

        assertThat(claims.getSubject()).isEqualTo("admin@workshop.com");
        assertThat(jwtService.isClientToken(claims)).isFalse();
    }

    @Test
    void recognisesServerlessClientToken() {
        Claims claims = jwtService.parseClaims(clientToken(3_600_000L));

        assertThat(jwtService.isClientToken(claims)).isTrue();
        assertThat(claims.getSubject()).isEqualTo("52998224725");
    }

    @Test
    void rejectsMalformedToken() {
        assertThatThrownBy(() -> jwtService.parseClaims("not-a-jwt"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsExpiredToken() {
        assertThatThrownBy(() -> jwtService.parseClaims(clientToken(-1_000L)))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsForeignSignature() {
        String foreign = Jwts.builder()
                .subject("52998224725")
                .claim("clientId", 1)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor("a-totally-different-secret-value-still-long!!".getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThatThrownBy(() -> jwtService.parseClaims(foreign))
                .isInstanceOf(JwtException.class);
    }
}

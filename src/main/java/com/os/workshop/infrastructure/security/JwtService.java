package com.os.workshop.infrastructure.security;

import com.os.workshop.domain.user.Role;
import com.os.workshop.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    private final SecretKey key;
    private final JwtProperties props;

    public JwtService(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("roles", user.getRoles().stream().map(Role::getName).toList())
                .issuedAt(new Date(now))
                .expiration(new Date(now + props.getExpiration()))
                .signWith(key)
                .compact();
    }

    public boolean isValid(String token, UserDetails userDetails) {
        try {
            final String usernameFromToken = extractUsername(token);
            return (usernameFromToken.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) { return getClaims(token).getSubject(); }

    /** Safely parses and verifies a token; empty when invalid/expired. */
    public java.util.Optional<Claims> tryParseClaims(String token) {
        try {
            return java.util.Optional.of(getClaims(token));
        } catch (Exception e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return java.util.Optional.empty();
        }
    }

    /** A client (CPF) token issued by the auth Lambda carries a clientId claim. */
    public boolean isClientToken(Claims claims) {
        return claims.get("clientId") != null;
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}

package com.os.features.user.security.jwt;

import com.os.features.user.domain.Role;
import com.os.features.user.domain.User;
import com.os.features.user.security.config.JwtProperties;
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

        log.debug("Generating JWT Token for user {}", user.getEmail());

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("roles", user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .toList())
                .issuedAt(new Date(now))
                .expiration(new Date(now + props.getExpiration()))
                .signWith(key)
                .compact();
    }

    public boolean isValid(String token, UserDetails userDetails) {

        log.debug("Validating JWT Token for user {}", userDetails.getUsername());

        final String usernameFromToken = extractUsername(token);

        return (usernameFromToken.equals(userDetails.getUsername())
                && !isTokenExpired(token));
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    private boolean isTokenExpired(String token) {
        log.debug("Checking JWT Token for user {}", token);
        return getClaims(token)
                .getExpiration()
                .before(new java.util.Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

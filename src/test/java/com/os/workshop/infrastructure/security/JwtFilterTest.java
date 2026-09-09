package com.os.workshop.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtFilterTest {

    private static final String SECRET = "test-secret-that-is-long-enough-for-hs256-signing!!";

    private final JwtService jwtService = new JwtService(new JwtProperties(SECRET, 3_600_000L));
    private final CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
    private final JwtFilter filter = new JwtFilter(jwtService, userDetailsService);
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest requestWith(String bearer) {
        var request = new MockHttpServletRequest();
        if (bearer != null) {
            request.addHeader("Authorization", "Bearer " + bearer);
        }
        return request;
    }

    private void run(MockHttpServletRequest request) throws Exception {
        var chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        assertThat(chain.getRequest()).as("filter chain must always continue").isNotNull();
    }

    private String clientToken() {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject("52998224725")
                .claim("clientId", 1)
                .claim("name", "JOAO")
                .claim("roles", List.of("CLIENT"))
                .issuedAt(new Date(now))
                .expiration(new Date(now + 60_000))
                .signWith(key)
                .compact();
    }

    @Test
    void serverlessClientTokenIsAuthenticatedAsRoleClientWithoutDbLookup() throws Exception {
        run(requestWith(clientToken()));

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("52998224725");
        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_CLIENT");
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void staffTokenAuthoritiesComeFromTheDatabase() throws Exception {
        when(userDetailsService.loadUserByUsername("admin@workshop.com")).thenReturn(
                new User("admin@workshop.com", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        long now = System.currentTimeMillis();
        String staff = Jwts.builder()
                .subject("admin@workshop.com")
                .claim("userId", "u-1")
                .claim("roles", List.of("ROLE_ADMIN"))
                .issuedAt(new Date(now))
                .expiration(new Date(now + 60_000))
                .signWith(key)
                .compact();

        run(requestWith(staff));

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_ADMIN");
    }

    @Test
    void unknownStaffUserLeavesContextAnonymousWithoutThrowing() throws Exception {
        when(userDetailsService.loadUserByUsername(anyString()))
                .thenThrow(new UsernameNotFoundException("nope"));

        long now = System.currentTimeMillis();
        String staff = Jwts.builder().subject("ghost@workshop.com")
                .issuedAt(new Date(now)).expiration(new Date(now + 60_000))
                .signWith(key).compact();

        run(requestWith(staff));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void malformedTokenLeavesContextAnonymous() throws Exception {
        run(requestWith("garbage.token.value"));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void missingHeaderPassesThrough() throws Exception {
        run(requestWith(null));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}

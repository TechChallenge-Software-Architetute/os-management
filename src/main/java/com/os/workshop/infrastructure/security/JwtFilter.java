package com.os.workshop.infrastructure.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates requests from a {@code Bearer} JWT.
 *
 * <p>Two token shapes are accepted, both signed with the shared {@code jwt.secret} (HS256):
 * <ul>
 *   <li><b>Staff token</b> — issued by {@code POST /auth/login}. Subject is the e-mail; the
 *       authorities come from the {@code users} table (never from the token).</li>
 *   <li><b>Client token</b> — issued by the serverless CPF authentication function
 *       (os-management-lambda) and validated at the API Gateway. Subject is the client's
 *       CPF/CNPJ, it carries a {@code clientId} claim and there is <i>no</i> {@code users}
 *       row. It is granted the single authority {@code ROLE_CLIENT}.</li>
 * </ul>
 *
 * <p>An absent, malformed, tampered or expired token leaves the context anonymous and lets
 * Spring Security answer {@code 401/403} — it never throws out of the filter.
 */
@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims;
        try {
            claims = jwtService.parseClaims(authHeader.substring(7));
        } catch (Exception e) {
            log.debug("Rejected JWT: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = jwtService.isClientToken(claims)
                ? clientAuthentication(claims)
                : staffAuthentication(claims);

        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    /** CPF/CNPJ token from the serverless auth function — no DB lookup, fixed {@code ROLE_CLIENT}. */
    private UsernamePasswordAuthenticationToken clientAuthentication(Claims claims) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_CLIENT"));
        var principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    /** Staff token — authorities are resolved from the {@code users} table by e-mail. */
    private UsernamePasswordAuthenticationToken staffAuthentication(Claims claims) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        } catch (UsernameNotFoundException e) {
            log.debug("JWT subject is not a known user: {}", claims.getSubject());
            return null;
        }
    }
}

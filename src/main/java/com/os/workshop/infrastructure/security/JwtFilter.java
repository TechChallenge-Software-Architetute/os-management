package com.os.workshop.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
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
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            jwtService.tryParseClaims(token).ifPresent(claims -> {
                if (jwtService.isClientToken(claims)) {
                    authenticateClient(claims);
                } else {
                    authenticateUser(token, claims.getSubject());
                }
            });
        }
        filterChain.doFilter(request, response);
    }

    // Client (CPF) token: trust the verified claims, grant ROLE_CLIENT.
    // Principal username is the CPF (token subject); no users-table lookup.
    private void authenticateClient(io.jsonwebtoken.Claims claims) {
        var authorities = java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENT"));
        UserDetails principal = new org.springframework.security.core.userdetails.User(
                claims.getSubject(), "", authorities);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // Staff (email) token: resolve against the users table as before.
    private void authenticateUser(String token, String username) {
        if (username == null) {
            return;
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (jwtService.isValid(token, userDetails)) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }
}

package com.os.workshop.user.security.filter;

import com.os.workshop.user.security.jwt.JwtService;
import com.os.workshop.user.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JwtFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
    private final FilterChain filterChain = mock(FilterChain.class);
    private final JwtFilter filter = new JwtFilter(jwtService, userDetailsService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void skipsRequestWithoutBearerToken() throws Exception {
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), filterChain);

        verifyNoInteractions(userDetailsService);
    }

    @Test
    void authenticatesValidBearerToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername("a@b.com")
                .password("pwd")
                .authorities("ROLE_ADMIN")
                .build();

        when(jwtService.extractUsername("token")).thenReturn("a@b.com");
        when(userDetailsService.loadUserByUsername("a@b.com")).thenReturn(userDetails);
        when(jwtService.isValid("token", userDetails)).thenReturn(true);

        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

package com.os.workshop.user.security.service;

import com.os.workshop.features.user.domain.Role;
import com.os.workshop.features.user.domain.User;
import com.os.workshop.features.user.repository.UserRepository;
import com.os.workshop.features.user.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CustomUserDetailsService service = new CustomUserDetailsService(userRepository);

    @Test
    void loadsUserAuthorities() {
        User user = new User(UUID.randomUUID(), "a@b.com", "pwd", Set.of(new Role("ADMIN")), Set.of());

        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("a@b.com");

        assertEquals("a@b.com", details.getUsername());
        assertTrue(details.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void throwsWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@b.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing@b.com"));
    }
}

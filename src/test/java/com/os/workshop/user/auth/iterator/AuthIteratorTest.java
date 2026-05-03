package com.os.workshop.user.auth.iterator;

import com.os.workshop.user.domain.Role;
import com.os.workshop.user.domain.User;
import com.os.workshop.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthIteratorTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuthIterator iterator = new AuthIterator(userRepository);

    @Test
    void returnsUserWithoutPassword() {
        User user = new User(UUID.randomUUID(), "a@b.com", "pwd", Set.of(new Role("ADMIN")), Set.of());

        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        User result = iterator.getUserByEmail("a@b.com");

        assertEquals("a@b.com", result.getEmail());
        assertNull(result.getPassword());
    }
}

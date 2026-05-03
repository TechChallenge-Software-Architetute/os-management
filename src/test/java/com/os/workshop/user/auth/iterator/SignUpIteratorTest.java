package com.os.workshop.user.auth.iterator;

import com.os.workshop.user.dto.SignUpRequest;
import com.os.workshop.user.dto.SignUpResponse;
import com.os.workshop.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SignUpIteratorTest {

    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SignUpIterator iterator = new SignUpIterator(passwordEncoder, userRepository);

    @Test
    void encodesPasswordAndDelegatesSave() {
        SignUpRequest request = new SignUpRequest("a@b.com", "raw", Set.of("ADMIN"));
        SignUpResponse response = new SignUpResponse(UUID.randomUUID(), "a@b.com", Set.of("ROLE_ADMIN"));

        when(passwordEncoder.encode("raw")).thenReturn("encoded");
        when(userRepository.save(request, "encoded")).thenReturn(response);

        assertEquals(response, iterator.signUp(request));
    }
}

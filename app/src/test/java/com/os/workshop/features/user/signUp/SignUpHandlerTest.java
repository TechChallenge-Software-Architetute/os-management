package com.os.workshop.features.user.signUp;

import com.os.workshop.features.user.shared.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SignUpHandlerTest {

    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SignUpHandler handler = new SignUpHandler(passwordEncoder, userRepository);

    @Test
    void encodesPasswordAndDelegatesSave() {
        SignUpRequest request = new SignUpRequest("a@b.com", "raw", Set.of("ADMIN"));
        SignUpResponse response = new SignUpResponse(UUID.randomUUID(), "a@b.com", Set.of("ROLE_ADMIN"));

        when(passwordEncoder.encode("raw")).thenReturn("encoded");
        when(userRepository.save(request, "encoded")).thenReturn(response);

        assertEquals(response, handler.handle(request));
    }
}

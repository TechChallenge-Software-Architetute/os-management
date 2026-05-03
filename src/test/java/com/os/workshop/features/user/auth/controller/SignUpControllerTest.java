package com.os.workshop.features.user.auth.controller;

import com.os.workshop.features.user.auth.controller.SignUpController;
import com.os.workshop.features.user.auth.iterator.SignUpIterator;
import com.os.workshop.features.user.dto.SignUpRequest;
import com.os.workshop.features.user.dto.SignUpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SignUpControllerTest {

    private final SignUpIterator iterator = mock(SignUpIterator.class);
    private final SignUpController controller = new SignUpController(iterator);

    @Test
    void returnsIteratorResponse() {
        SignUpRequest request = new SignUpRequest("a@b.com", "raw", Set.of("ADMIN"));
        SignUpResponse response = new SignUpResponse(UUID.randomUUID(), "a@b.com", Set.of("ROLE_ADMIN"));

        when(iterator.signUp(request)).thenReturn(response);

        var result = controller.signUp(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }
}

package com.os.workshop.features.client.shared.exception;

import com.os.workshop.domain.client.ClientNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientNotFoundExceptionTest {

    @Test
    void messageContainsIdentifier() {
        assertTrue(new ClientNotFoundException("123").getMessage().contains("123"));
    }
}

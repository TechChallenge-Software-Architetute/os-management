package com.os.workshop.vehicle.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleNotFoundExceptionTest {

    @Test
    void messageContainsIdentifier() {
        assertTrue(new VehicleNotFoundException("ABC1234").getMessage().contains("ABC1234"));
    }
}

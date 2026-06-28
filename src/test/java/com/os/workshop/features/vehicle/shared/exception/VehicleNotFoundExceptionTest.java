package com.os.workshop.features.vehicle.shared.exception;

import com.os.workshop.domain.vehicle.VehicleNotFoundException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleNotFoundExceptionTest {

    @Test
    void messageContainsIdentifier() {
        assertTrue(new VehicleNotFoundException("ABC1234").getMessage().contains("ABC1234"));
    }
}

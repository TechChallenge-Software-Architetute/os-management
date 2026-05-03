package com.os.workshop.features.vehicle.dto;

import com.os.workshop.features.vehicle.domain.Vehicle;
import com.os.workshop.features.vehicle.domain.VehicleType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleResponseTest {

    @Test
    void formatsPlateFromVehicle() {
        LocalDateTime now = LocalDateTime.now();
        Vehicle vehicle = Vehicle.reconstitute(2L, 1L, "ABC1234", "Toyota", "Corolla", 2020, "Preto", VehicleType.CAR, true, now, now);

        VehicleResponse response = VehicleResponse.from(vehicle);

        assertEquals("ABC-1234", response.plate());
        assertTrue(response.active());
    }
}

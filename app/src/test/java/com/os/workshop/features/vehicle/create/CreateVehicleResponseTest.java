package com.os.workshop.features.vehicle.create;

import com.os.workshop.features.vehicle.shared.domain.Vehicle;
import com.os.workshop.features.vehicle.shared.domain.VehicleType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateVehicleResponseTest {

    @Test
    void formatsPlateFromVehicle() {
        LocalDateTime now = LocalDateTime.now();
        Vehicle vehicle = Vehicle.reconstitute(2L, 1L, "ABC1234", "Toyota", "Corolla", 2020, "Preto", VehicleType.CAR, true, now, now);

        CreateVehicleResponse response = CreateVehicleResponse.from(vehicle);

        assertEquals("ABC-1234", response.plate());
        assertTrue(response.active());
    }
}

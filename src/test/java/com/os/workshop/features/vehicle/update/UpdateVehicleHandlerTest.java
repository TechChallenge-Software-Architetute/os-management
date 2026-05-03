package com.os.workshop.features.vehicle.update;

import com.os.workshop.features.vehicle.shared.domain.Vehicle;
import com.os.workshop.features.vehicle.shared.domain.VehicleType;
import com.os.workshop.features.vehicle.shared.exception.VehicleNotFoundException;
import com.os.workshop.features.vehicle.shared.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateVehicleHandlerTest {

    @Mock private VehicleRepository vehicleRepository;
    @InjectMocks private UpdateVehicleHandler handler;

    private Vehicle createVehicle() {
        return Vehicle.reconstitute(1L, 1L, "ABC1234", "TOYOTA", "COROLLA", 2020, "WHITE", VehicleType.CAR, true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void updatesVehicleSuccessfully() {
        Vehicle vehicle = createVehicle();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));
        var request = new UpdateVehicleRequest("DEF5678", "Honda", "Civic", 2022, "Black", VehicleType.CAR);
        var result = handler.handle(1L, request);
        assertEquals("DEF5678", result.getPlate().getValue());
    }

    @Test
    void throwsWhenVehicleNotFound() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());
        var request = new UpdateVehicleRequest("ABC1234", "X", "X", 2020, "X", VehicleType.CAR);
        assertThrows(VehicleNotFoundException.class, () -> handler.handle(99L, request));
    }

    @Test
    void throwsWhenDuplicatePlate() {
        Vehicle vehicle = createVehicle();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.existsByPlate("DEF5678")).thenReturn(true);
        var request = new UpdateVehicleRequest("DEF5678", "Honda", "Civic", 2022, "Black", VehicleType.CAR);
        assertThrows(IllegalStateException.class, () -> handler.handle(1L, request));
    }
}

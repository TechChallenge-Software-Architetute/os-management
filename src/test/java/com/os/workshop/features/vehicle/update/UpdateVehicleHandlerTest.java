package com.os.workshop.features.vehicle.update;

import com.os.workshop.domain.vehicle.Vehicle;
import com.os.workshop.domain.vehicle.VehicleType;
import com.os.workshop.domain.vehicle.VehicleNotFoundException;
import com.os.workshop.application.vehicle.port.out.VehicleRepository;
import com.os.workshop.application.vehicle.UpdateVehicleUseCase;
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
class UpdateVehicleUseCaseTest {

    @Mock private VehicleRepository vehicleRepository;
    @InjectMocks private UpdateVehicleUseCase handler;

    private Vehicle createVehicle() {
        return Vehicle.reconstitute(1L, 1L, "ABC1234", "TOYOTA", "COROLLA", 2020, "WHITE", VehicleType.CAR, true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void updatesVehicleSuccessfully() {
        Vehicle vehicle = createVehicle();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));
        var result = handler.execute(1L, "DEF5678", "Honda", "Civic", 2022, "Black", VehicleType.CAR);
        assertEquals("DEF5678", result.getPlate().getValue());
    }

    @Test
    void throwsWhenVehicleNotFound() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(VehicleNotFoundException.class,
                () -> handler.execute(99L, "ABC1234", "X", "X", 2020, "X", VehicleType.CAR));
    }

    @Test
    void throwsWhenDuplicatePlate() {
        Vehicle vehicle = createVehicle();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.existsByPlate("DEF5678")).thenReturn(true);
        assertThrows(IllegalStateException.class,
                () -> handler.execute(1L, "DEF5678", "Honda", "Civic", 2022, "Black", VehicleType.CAR));
    }
}

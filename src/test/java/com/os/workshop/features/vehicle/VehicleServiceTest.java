package com.os.workshop.features.vehicle;

import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.vehicle.CreateVehicleUseCase;
import com.os.workshop.application.vehicle.DeactivateVehicleUseCase;
import com.os.workshop.application.vehicle.FindVehicleByIdUseCase;
import com.os.workshop.application.vehicle.FindVehicleByPlateUseCase;
import com.os.workshop.domain.vehicle.Vehicle;
import com.os.workshop.domain.vehicle.VehicleType;
import com.os.workshop.domain.vehicle.VehicleNotFoundException;
import com.os.workshop.application.vehicle.port.out.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private ClientRepository clientRepository;

    @InjectMocks private CreateVehicleUseCase createVehicleUseCase;
    @InjectMocks private FindVehicleByIdUseCase findVehicleByIdUseCase;
    @InjectMocks private FindVehicleByPlateUseCase findVehicleByPlateUseCase;
    @InjectMocks private DeactivateVehicleUseCase deactivateVehicleUseCase;

    private Vehicle createVehicle() {
        return Vehicle.reconstitute(1L, 1L, "ABC1234", "TOYOTA", "COROLLA",
                2020, "WHITE", VehicleType.CAR, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Client createClient() {
        return Client.reconstitute(1L, "JOHN DOE", "52998224725",
                "john@email.com", "11999999999", true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void whenCreatingVehicleWithValidData_thenVehicleIsSaved() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(createClient()));
        when(vehicleRepository.existsByPlate("ABC1234")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(createVehicle());

        Vehicle result = createVehicleUseCase.execute(1L, "ABC1234", "Toyota", "Corolla", 2020, "White", VehicleType.CAR);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void whenCreatingVehicleWithNonExistingClient_thenThrowsClientNotFound() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ClientNotFoundException.class,
                () -> createVehicleUseCase.execute(1L, "ABC1234", "Toyota", "Corolla", 2020, "White", VehicleType.CAR));
    }

    @Test
    void whenCreatingVehicleWithDuplicatePlate_thenThrowsIllegalState() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(createClient()));
        when(vehicleRepository.existsByPlate("ABC1234")).thenReturn(true);
        assertThrows(IllegalStateException.class,
                () -> createVehicleUseCase.execute(1L, "ABC1234", "Toyota", "Corolla", 2020, "White", VehicleType.CAR));
    }

    @Test
    void whenFindingVehicleByExistingId_thenReturnsVehicle() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(createVehicle()));
        assertEquals(1L, findVehicleByIdUseCase.execute(1L).getId());
    }

    @Test
    void whenFindingVehicleByNonExistingId_thenThrowsVehicleNotFound() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(VehicleNotFoundException.class, () -> findVehicleByIdUseCase.execute(999L));
    }

    @Test
    void whenFindingVehicleByExistingPlate_thenReturnsVehicle() {
        when(vehicleRepository.findByPlate("ABC1234")).thenReturn(Optional.of(createVehicle()));
        assertEquals("ABC1234", findVehicleByPlateUseCase.execute("ABC1234").getPlate().getValue());
    }

    @Test
    void whenDeactivatingVehicle_thenVehicleIsDeactivated() {
        Vehicle vehicle = createVehicle();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        deactivateVehicleUseCase.execute(1L);
        assertFalse(vehicle.isActive());
        verify(vehicleRepository).save(vehicle);
    }
}

package com.os.workshop.features.vehicle;

import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.features.vehicle.create.CreateVehicleHandler;
import com.os.workshop.features.vehicle.create.CreateVehicleRequest;
import com.os.workshop.features.vehicle.deactivate.DeactivateVehicleHandler;
import com.os.workshop.features.vehicle.findById.FindVehicleByIdHandler;
import com.os.workshop.features.vehicle.findByPlate.FindVehicleByPlateHandler;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private ClientRepository clientRepository;

    @InjectMocks private CreateVehicleHandler createVehicleHandler;
    @InjectMocks private FindVehicleByIdHandler findVehicleByIdHandler;
    @InjectMocks private FindVehicleByPlateHandler findVehicleByPlateHandler;
    @InjectMocks private DeactivateVehicleHandler deactivateVehicleHandler;

    private CreateVehicleRequest createRequest() {
        return new CreateVehicleRequest(1L, "ABC1234", "Toyota", "Corolla",
                2020, "White", VehicleType.CAR);
    }

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

        Vehicle result = createVehicleHandler.handle(createRequest());

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void whenCreatingVehicleWithNonExistingClient_thenThrowsClientNotFound() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());
        var result = createRequest();
        assertThrows(ClientNotFoundException.class, () -> createVehicleHandler.handle(result));
    }

    @Test
    void whenCreatingVehicleWithDuplicatePlate_thenThrowsIllegalState() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(createClient()));
        when(vehicleRepository.existsByPlate("ABC1234")).thenReturn(true);
        var result = createRequest();
        assertThrows(IllegalStateException.class, () -> createVehicleHandler.handle(result));
    }

    @Test
    void whenFindingVehicleByExistingId_thenReturnsVehicle() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(createVehicle()));
        assertEquals(1L, findVehicleByIdHandler.handle(1L).getId());
    }

    @Test
    void whenFindingVehicleByNonExistingId_thenThrowsVehicleNotFound() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(VehicleNotFoundException.class, () -> findVehicleByIdHandler.handle(999L));
    }

    @Test
    void whenFindingVehicleByExistingPlate_thenReturnsVehicle() {
        when(vehicleRepository.findByPlate("ABC1234")).thenReturn(Optional.of(createVehicle()));
        assertEquals("ABC1234", findVehicleByPlateHandler.handle("ABC1234").getPlate().getValue());
    }

    @Test
    void whenDeactivatingVehicle_thenVehicleIsDeactivated() {
        Vehicle vehicle = createVehicle();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        deactivateVehicleHandler.handle(1L);
        assertFalse(vehicle.isActive());
        verify(vehicleRepository).save(vehicle);
    }
}

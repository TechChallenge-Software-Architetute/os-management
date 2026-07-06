package com.os.workshop.application.vehicle;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.vehicle.port.out.VehicleRepository;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.domain.vehicle.Vehicle;
import com.os.workshop.domain.vehicle.VehicleNotFoundException;
import com.os.workshop.domain.vehicle.VehicleType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleUseCaseTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private ClientRepository clientRepository;

    @InjectMocks private CreateVehicleUseCase createVehicleUseCase;
    @InjectMocks private FindVehicleByIdUseCase findVehicleByIdUseCase;
    @InjectMocks private FindVehicleByPlateUseCase findVehicleByPlateUseCase;
    @InjectMocks private FindVehiclesByClientUseCase findVehiclesByClientUseCase;
    @InjectMocks private DeactivateVehicleUseCase deactivateVehicleUseCase;
    @InjectMocks private UpdateVehicleUseCase updateVehicleUseCase;

    private Vehicle reconstitute(Long id, String plate) {
        return Vehicle.reconstitute(id, 1L, plate, "FIAT", "UNO", 2020, "WHITE",
                VehicleType.CAR, true, LocalDateTime.now(), LocalDateTime.now());
    }

    // ==================== CreateVehicleUseCase ====================

    @Nested
    class CreateVehicle {

        @Test
        void createsVehicleSuccessfully() {
            when(clientRepository.findById(1L)).thenReturn(Optional.of(mock(Client.class)));
            when(vehicleRepository.existsByPlate("ABC1D23")).thenReturn(false);
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

            Vehicle result = createVehicleUseCase.execute(1L, "ABC-1D23", "Fiat", "Uno", 2020, "White", VehicleType.CAR);

            assertNotNull(result);
            assertEquals("ABC1D23", result.getPlate().getValue());
            verify(vehicleRepository).save(any(Vehicle.class));
        }

        @Test
        void throwsClientNotFoundWhenClientDoesNotExist() {
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ClientNotFoundException.class,
                    () -> createVehicleUseCase.execute(99L, "ABC-1234", "Fiat", "Uno", 2020, "White", VehicleType.CAR));
            verify(vehicleRepository, never()).save(any());
        }

        @Test
        void throwsWhenPlateAlreadyExists() {
            when(clientRepository.findById(1L)).thenReturn(Optional.of(mock(Client.class)));
            when(vehicleRepository.existsByPlate("ABC1234")).thenReturn(true);

            assertThrows(IllegalStateException.class,
                    () -> createVehicleUseCase.execute(1L, "ABC-1234", "Fiat", "Uno", 2020, "White", VehicleType.CAR));
            verify(vehicleRepository, never()).save(any());
        }
    }

    // ==================== FindVehicleByIdUseCase ====================

    @Nested
    class FindVehicleById {

        @Test
        void returnsVehicleWhenFound() {
            Vehicle vehicle = reconstitute(1L, "ABC1234");
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

            Vehicle result = findVehicleByIdUseCase.execute(1L);

            assertEquals(1L, result.getId());
            assertEquals("ABC1234", result.getPlate().getValue());
        }

        @Test
        void throwsVehicleNotFoundWhenIdDoesNotExist() {
            when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(VehicleNotFoundException.class,
                    () -> findVehicleByIdUseCase.execute(99L));
        }
    }

    // ==================== FindVehicleByPlateUseCase ====================

    @Nested
    class FindVehicleByPlate {

        @Test
        void returnsVehicleWhenPlateFound() {
            Vehicle vehicle = reconstitute(1L, "ABC1D23");
            when(vehicleRepository.findByPlate("ABC1D23")).thenReturn(Optional.of(vehicle));

            Vehicle result = findVehicleByPlateUseCase.execute("abc-1d23");

            assertEquals("ABC1D23", result.getPlate().getValue());
        }

        @Test
        void normalizesPlateBeforeSearching() {
            Vehicle vehicle = reconstitute(1L, "XYZ9876");
            when(vehicleRepository.findByPlate("XYZ9876")).thenReturn(Optional.of(vehicle));

            Vehicle result = findVehicleByPlateUseCase.execute("xyz-9876");

            assertEquals("XYZ9876", result.getPlate().getValue());
            verify(vehicleRepository).findByPlate("XYZ9876");
        }

        @Test
        void throwsVehicleNotFoundWhenPlateDoesNotExist() {
            when(vehicleRepository.findByPlate("ZZZ0000")).thenReturn(Optional.empty());

            assertThrows(VehicleNotFoundException.class,
                    () -> findVehicleByPlateUseCase.execute("ZZZ-0000"));
        }
    }

    // ==================== FindVehiclesByClientUseCase ====================

    @Nested
    class FindVehiclesByClient {

        @Test
        void returnsVehiclesForClient() {
            Vehicle v1 = reconstitute(1L, "ABC1234");
            Vehicle v2 = reconstitute(2L, "DEF5G78");
            when(clientRepository.findById(1L)).thenReturn(Optional.of(mock(Client.class)));
            when(vehicleRepository.findAllByClientId(1L)).thenReturn(List.of(v1, v2));

            List<Vehicle> result = findVehiclesByClientUseCase.execute(1L);

            assertEquals(2, result.size());
        }

        @Test
        void throwsClientNotFoundWhenClientDoesNotExist() {
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ClientNotFoundException.class,
                    () -> findVehiclesByClientUseCase.execute(99L));
            verify(vehicleRepository, never()).findAllByClientId(any());
        }

        @Test
        void returnsEmptyListWhenClientHasNoVehicles() {
            when(clientRepository.findById(1L)).thenReturn(Optional.of(mock(Client.class)));
            when(vehicleRepository.findAllByClientId(1L)).thenReturn(List.of());

            List<Vehicle> result = findVehiclesByClientUseCase.execute(1L);

            assertTrue(result.isEmpty());
        }
    }

    // ==================== DeactivateVehicleUseCase ====================

    @Nested
    class DeactivateVehicle {

        @Test
        void deactivatesVehicleSuccessfully() {
            Vehicle vehicle = reconstitute(1L, "ABC1234");
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

            deactivateVehicleUseCase.execute(1L);

            assertFalse(vehicle.isActive());
            verify(vehicleRepository).save(vehicle);
        }

        @Test
        void throwsVehicleNotFoundWhenIdDoesNotExist() {
            when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(VehicleNotFoundException.class,
                    () -> deactivateVehicleUseCase.execute(99L));
            verify(vehicleRepository, never()).save(any());
        }
    }

    // ==================== UpdateVehicleUseCase ====================

    @Nested
    class UpdateVehicle {

        @Test
        void updatesVehicleSuccessfully() {
            Vehicle vehicle = reconstitute(1L, "ABC1234");
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
            when(vehicleRepository.existsByPlate("DEF5G78")).thenReturn(false);
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

            Vehicle result = updateVehicleUseCase.execute(1L, "DEF-5G78", "Toyota", "Corolla", 2022, "Black", VehicleType.CAR);

            assertNotNull(result);
            verify(vehicleRepository).save(vehicle);
        }

        @Test
        void throwsVehicleNotFoundWhenIdDoesNotExist() {
            when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(VehicleNotFoundException.class,
                    () -> updateVehicleUseCase.execute(99L, "ABC-1234", "Fiat", "Uno", 2020, "White", VehicleType.CAR));
            verify(vehicleRepository, never()).save(any());
        }

        @Test
        void throwsWhenNewPlateAlreadyExistsOnDifferentVehicle() {
            Vehicle vehicle = reconstitute(1L, "ABC1234");
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
            when(vehicleRepository.existsByPlate("DEF5G78")).thenReturn(true);

            assertThrows(IllegalStateException.class,
                    () -> updateVehicleUseCase.execute(1L, "DEF-5G78", "Toyota", "Corolla", 2022, "Black", VehicleType.CAR));
            verify(vehicleRepository, never()).save(any());
        }

        @Test
        void allowsUpdateWhenPlateRemainsTheSame() {
            Vehicle vehicle = reconstitute(1L, "ABC1234");
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

            Vehicle result = updateVehicleUseCase.execute(1L, "ABC-1234", "Fiat", "Palio", 2021, "Red", VehicleType.CAR);

            assertNotNull(result);
            verify(vehicleRepository, never()).existsByPlate(any());
        }
    }
}

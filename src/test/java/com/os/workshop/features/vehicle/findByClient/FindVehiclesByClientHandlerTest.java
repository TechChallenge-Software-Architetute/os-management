package com.os.workshop.features.vehicle.findByClient;

import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.client.shared.exception.ClientNotFoundException;
import com.os.workshop.features.client.shared.repository.ClientRepository;
import com.os.workshop.features.vehicle.shared.domain.Vehicle;
import com.os.workshop.features.vehicle.shared.domain.VehicleType;
import com.os.workshop.features.vehicle.shared.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindVehiclesByClientHandlerTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private ClientRepository clientRepository;
    @InjectMocks private FindVehiclesByClientHandler handler;

    @Test
    void returnsVehiclesForExistingClient() {
        Client client = Client.reconstitute(1L, "JOHN", "52998224725", "j@e.com", "11999", true, LocalDateTime.now(), LocalDateTime.now());
        Vehicle vehicle = Vehicle.reconstitute(1L, 1L, "ABC1234", "TOYOTA", "COROLLA", 2020, "WHITE", VehicleType.CAR, true, LocalDateTime.now(), LocalDateTime.now());
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(vehicleRepository.findAllByClientId(1L)).thenReturn(List.of(vehicle));
        assertEquals(1, handler.handle(1L).size());
    }

    @Test
    void throwsWhenClientNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ClientNotFoundException.class, () -> handler.handle(99L));
    }
}

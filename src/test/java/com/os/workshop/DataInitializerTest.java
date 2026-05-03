package com.os.workshop;

import com.os.workshop.client.domain.Client;
import com.os.workshop.client.repository.ClientRepository;
import com.os.workshop.vehicle.domain.Vehicle;
import com.os.workshop.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataInitializerTest {

    private final ClientRepository clientRepository = mock(ClientRepository.class);
    private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
    private final DataInitializer dataInitializer = new DataInitializer(clientRepository, vehicleRepository);

    @Test
    void skipsSeedWhenActiveClientsExist() {
        when(clientRepository.findAllActive())
                .thenReturn(List.of(Client.reconstitute(1L, "ANA", "52998224725", "a@b.com", "999", true, null, null)));

        dataInitializer.run(null);

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void seedsClientsAndVehiclesWhenDatabaseIsEmpty() {
        Client joao = Client.reconstitute(1L, "JOAO", "52998224725", "joao@email.com", "999", true, null, null);
        Client maria = Client.reconstitute(2L, "MARIA", "07124632080", "maria@email.com", "999", true, null, null);
        Client carlos = Client.reconstitute(3L, "CARLOS", "18746880011", "carlos@email.com", "999", true, null, null);

        when(clientRepository.findAllActive()).thenReturn(List.of());
        when(clientRepository.save(any(Client.class))).thenReturn(joao, maria, carlos);

        dataInitializer.run(null);

        verify(clientRepository, times(3)).save(any(Client.class));
        verify(vehicleRepository, times(3)).save(any(Vehicle.class));
    }
}

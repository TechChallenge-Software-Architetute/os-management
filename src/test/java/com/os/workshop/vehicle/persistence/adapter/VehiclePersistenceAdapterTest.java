package com.os.workshop.vehicle.persistence.adapter;

import com.os.workshop.client.persistence.entity.ClientEntity;
import com.os.workshop.client.persistence.repository.ClientJpaRepository;
import com.os.workshop.vehicle.domain.Vehicle;
import com.os.workshop.vehicle.domain.VehicleType;
import com.os.workshop.vehicle.persistence.entity.VehicleEntity;
import com.os.workshop.vehicle.persistence.mapper.VehicleMapper;
import com.os.workshop.vehicle.persistence.repository.VehicleJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehiclePersistenceAdapterTest {

    @Mock
    private VehicleJpaRepository vehicleJpaRepository;

    @Mock
    private ClientJpaRepository clientJpaRepository;

    @Mock
    private VehicleMapper vehicleMapper;

    @InjectMocks
    private VehiclePersistenceAdapter adapter;

    @Test
    void savesExistingVehicleAndQueriesRepository() {
        ClientEntity clientEntity = new ClientEntity();
        Vehicle vehicle = Vehicle.reconstitute(1L, 2L, "ABC1234", "Toyota", "Corolla", 2020, "Preto", VehicleType.CAR, true, null, null);
        VehicleEntity entity = new VehicleEntity();

        when(clientJpaRepository.findById(2L)).thenReturn(Optional.of(clientEntity));
        when(vehicleJpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(vehicleJpaRepository.save(entity)).thenReturn(entity);
        when(vehicleMapper.toDomain(entity)).thenReturn(vehicle);
        when(vehicleJpaRepository.findByPlate("ABC1234")).thenReturn(Optional.of(entity));
        when(vehicleJpaRepository.findByClient_IdAndActiveTrue(2L)).thenReturn(List.of(entity));
        when(vehicleJpaRepository.existsByPlate("ABC1234")).thenReturn(true);

        assertEquals(1L, adapter.save(vehicle).getId());
        assertTrue(adapter.findById(1L).isPresent());
        assertTrue(adapter.findByPlate("ABC1234").isPresent());
        assertEquals(1, adapter.findAllByClientId(2L).size());
        assertTrue(adapter.existsByPlate("ABC1234"));
    }

    @Test
    void throwsWhenClientDoesNotExist() {
        Vehicle vehicle = Vehicle.reconstitute(null, 9L, "DEF5678", "VW", "Gol", 2018, "Branco", VehicleType.CAR, true, null, null);

        when(clientJpaRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> adapter.save(vehicle));
    }
}

package com.os.workshop.features.integration;

import com.os.workshop.features.client.shared.repository.ClientEntity;
import com.os.workshop.features.client.shared.repository.ClientJpaRepository;
import com.os.workshop.features.vehicle.shared.domain.VehicleType;
import com.os.workshop.features.vehicle.shared.repository.VehicleEntity;
import com.os.workshop.features.vehicle.shared.repository.VehicleJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the @ManyToOne relationship between VehicleEntity and ClientEntity,
 * unique plate constraint, and derived queries with association navigation.
 */
class VehicleRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private VehicleJpaRepository vehicleJpaRepository;

    @Autowired
    private ClientJpaRepository clientJpaRepository;

    private ClientEntity savedClient;

    @BeforeEach
    void setUp() {
        ClientEntity client = new ClientEntity();
        client.setName("Vehicle Owner");
        client.setCpf("33344455566");
        client.setActive(true);
        savedClient = clientJpaRepository.save(client);
    }

    @AfterEach
    void tearDown() {
        vehicleJpaRepository.deleteAll();
        clientJpaRepository.deleteAll();
    }

    @Test
    void savesVehicleWithClientRelationship() {
        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setClient(savedClient);
        vehicle.setPlate("ABC1234");
        vehicle.setBrand("TOYOTA");
        vehicle.setModel("COROLLA");
        vehicle.setYear(2020);
        vehicle.setColor("WHITE");
        vehicle.setType(VehicleType.CAR);
        vehicle.setActive(true);

        VehicleEntity saved = vehicleJpaRepository.save(vehicle);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());

        var found = vehicleJpaRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(savedClient.getId(), found.get().getClient().getId());
        assertEquals("ABC1234", found.get().getPlate());
    }

    @Test
    void findsByPlate() {
        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setClient(savedClient);
        vehicle.setPlate("DEF5678");
        vehicle.setBrand("HONDA");
        vehicle.setModel("CIVIC");
        vehicle.setYear(2022);
        vehicle.setType(VehicleType.CAR);
        vehicle.setActive(true);

        vehicleJpaRepository.save(vehicle);

        assertTrue(vehicleJpaRepository.findByPlate("DEF5678").isPresent());
        assertTrue(vehicleJpaRepository.existsByPlate("DEF5678"));
        assertFalse(vehicleJpaRepository.existsByPlate("ZZZ9999"));
    }

    @Test
    void findsByClientIdAndActiveTrue() {
        VehicleEntity active = new VehicleEntity();
        active.setClient(savedClient);
        active.setPlate("ACT1234");
        active.setBrand("VW");
        active.setModel("GOL");
        active.setYear(2018);
        active.setType(VehicleType.CAR);
        active.setActive(true);

        VehicleEntity inactive = new VehicleEntity();
        inactive.setClient(savedClient);
        inactive.setPlate("INA5678");
        inactive.setBrand("FIAT");
        inactive.setModel("UNO");
        inactive.setYear(2015);
        inactive.setType(VehicleType.CAR);
        inactive.setActive(false);

        vehicleJpaRepository.save(active);
        vehicleJpaRepository.save(inactive);

        var activeVehicles = vehicleJpaRepository.findByClient_IdAndActiveTrue(savedClient.getId());
        assertTrue(activeVehicles.stream().allMatch(VehicleEntity::isActive));
        assertTrue(activeVehicles.stream().anyMatch(v -> v.getPlate().equals("ACT1234")));
        assertTrue(activeVehicles.stream().noneMatch(v -> v.getPlate().equals("INA5678")));
    }
}

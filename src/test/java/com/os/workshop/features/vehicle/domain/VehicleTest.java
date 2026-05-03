package com.os.workshop.features.vehicle.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleTest {

    @Test
    void whenCreatingVehicleWithValidData_thenVehicleIsCreated() {
        Vehicle vehicle = Vehicle.create(1L, "ABC1234", "Toyota", "Corolla",
                2020, "White", VehicleType.CAR);

        assertNotNull(vehicle);
        assertEquals(1L, vehicle.getClientId());
        assertEquals("ABC1234", vehicle.getPlate().getValue());
        assertEquals("TOYOTA", vehicle.getBrand());
        assertEquals("COROLLA", vehicle.getModel());
        assertEquals(2020, vehicle.getYear());
        assertEquals("WHITE", vehicle.getColor());
        assertEquals(VehicleType.CAR, vehicle.getType());
        assertTrue(vehicle.isActive());
    }

    @Test
    void whenCreatingVehicleWithNullClientId_thenThrowsNullPointer() {
        assertThrows(NullPointerException.class,
                () -> Vehicle.create(null, "ABC1234", "Toyota", "Corolla",
                        2020, "White", VehicleType.CAR));
    }

    @Test
    void whenCreatingVehicleWithBlankBrand_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> Vehicle.create(1L, "ABC1234", " ", "Corolla",
                        2020, "White", VehicleType.CAR));
    }

    @Test
    void whenCreatingVehicleWithBlankModel_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> Vehicle.create(1L, "ABC1234", "Toyota", " ",
                        2020, "White", VehicleType.CAR));
    }

    @Test
    void whenCreatingVehicleWithYearBelow1886_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> Vehicle.create(1L, "ABC1234", "Toyota", "Corolla",
                        1885, "White", VehicleType.CAR));
    }

    @Test
    void whenUpdatingVehicle_thenFieldsAreUpdated() {
        Vehicle vehicle = Vehicle.create(1L, "ABC1234", "Toyota", "Corolla",
                2020, "White", VehicleType.CAR);

        vehicle.update("DEF5678", "Honda", "Civic", 2022, "Black", VehicleType.CAR);

        assertEquals("DEF5678", vehicle.getPlate().getValue());
        assertEquals("Honda", vehicle.getBrand());
        assertEquals("Civic", vehicle.getModel());
        assertEquals(2022, vehicle.getYear());
        assertEquals("Black", vehicle.getColor());
        assertEquals(VehicleType.CAR, vehicle.getType());
    }

    @Test
    void whenDeactivatingVehicle_thenActiveIsFalse() {
        Vehicle vehicle = Vehicle.create(1L, "ABC1234", "Toyota", "Corolla",
                2020, "White", VehicleType.CAR);

        vehicle.deactivate();

        assertFalse(vehicle.isActive());
    }
}

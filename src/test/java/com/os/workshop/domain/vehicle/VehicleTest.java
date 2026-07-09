package com.os.workshop.domain.vehicle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    // ==================== create ====================

    @Test
    void create_withValidData_returnsActiveVehicle() {
        Vehicle vehicle = Vehicle.create(1L, "ABC1234", "Toyota", "Corolla",
                2020, "White", VehicleType.CAR);

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
    void create_withMercosulPlate_succeeds() {
        Vehicle vehicle = Vehicle.create(1L, "ABC1D23", "Honda", "Civic",
                2022, "Black", VehicleType.CAR);

        assertEquals("ABC1D23", vehicle.getPlate().getValue());
    }

    @Test
    void create_withNullClientId_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                Vehicle.create(null, "ABC1234", "Toyota", "Corolla",
                        2020, "White", VehicleType.CAR));
    }

    @Test
    void create_withNullType_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                Vehicle.create(1L, "ABC1234", "Toyota", "Corolla",
                        2020, "White", null));
    }

    @Test
    void create_withBlankBrand_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                Vehicle.create(1L, "ABC1234", "  ", "Corolla",
                        2020, "White", VehicleType.CAR));
    }

    @Test
    void create_withNullBrand_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                Vehicle.create(1L, "ABC1234", null, "Corolla",
                        2020, "White", VehicleType.CAR));
    }

    @Test
    void create_withBlankModel_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                Vehicle.create(1L, "ABC1234", "Toyota", "",
                        2020, "White", VehicleType.CAR));
    }

    @Test
    void create_withNullModel_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                Vehicle.create(1L, "ABC1234", "Toyota", null,
                        2020, "White", VehicleType.CAR));
    }

    @Test
    void create_withYearBelow1886_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                Vehicle.create(1L, "ABC1234", "Toyota", "Corolla",
                        1885, "White", VehicleType.CAR));
    }

    @Test
    void create_withYear1886_succeeds() {
        Vehicle vehicle = Vehicle.create(1L, "ABC1234", "Benz", "Patent-Motorwagen",
                1886, "Black", VehicleType.CAR);

        assertEquals(1886, vehicle.getYear());
    }

    @Test
    void create_withInvalidPlate_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                Vehicle.create(1L, "INVALID", "Toyota", "Corolla",
                        2020, "White", VehicleType.CAR));
    }

    // ==================== update ====================

    @Test
    void update_withValidData_updatesFields() {
        Vehicle vehicle = Vehicle.create(1L, "ABC1234", "Toyota", "Corolla",
                2020, "White", VehicleType.CAR);

        vehicle.update("DEF5678", "Honda", "Civic", 2022, "Black", VehicleType.CAR);

        assertEquals("DEF5678", vehicle.getPlate().getValue());
        assertEquals("Honda", vehicle.getBrand());
        assertEquals("Civic", vehicle.getModel());
        assertEquals(2022, vehicle.getYear());
        assertEquals("Black", vehicle.getColor());
    }

    @Test
    void update_withNullType_throwsNullPointerException() {
        Vehicle vehicle = Vehicle.create(1L, "ABC1234", "Toyota", "Corolla",
                2020, "White", VehicleType.CAR);

        assertThrows(NullPointerException.class, () ->
                vehicle.update("DEF5678", "Honda", "Civic", 2022, "Black", null));
    }

    @Test
    void update_withBlankBrand_throwsIllegalArgument() {
        Vehicle vehicle = Vehicle.create(1L, "ABC1234", "Toyota", "Corolla",
                2020, "White", VehicleType.CAR);

        assertThrows(IllegalArgumentException.class, () ->
                vehicle.update("DEF5678", " ", "Civic", 2022, "Black", VehicleType.CAR));
    }

    @Test
    void update_withBlankModel_throwsIllegalArgument() {
        Vehicle vehicle = Vehicle.create(1L, "ABC1234", "Toyota", "Corolla",
                2020, "White", VehicleType.CAR);

        assertThrows(IllegalArgumentException.class, () ->
                vehicle.update("DEF5678", "Honda", "  ", 2022, "Black", VehicleType.CAR));
    }

    @Test
    void update_withYearBelow1886_throwsIllegalArgument() {
        Vehicle vehicle = Vehicle.create(1L, "ABC1234", "Toyota", "Corolla",
                2020, "White", VehicleType.CAR);

        assertThrows(IllegalArgumentException.class, () ->
                vehicle.update("DEF5678", "Honda", "Civic", 1800, "Black", VehicleType.CAR));
    }

    // ==================== deactivate ====================

    @Test
    void deactivate_setsActiveToFalse() {
        Vehicle vehicle = Vehicle.create(1L, "ABC1234", "Toyota", "Corolla",
                2020, "White", VehicleType.CAR);
        assertTrue(vehicle.isActive());

        vehicle.deactivate();

        assertFalse(vehicle.isActive());
    }

    @Test
    void deactivate_canBeCalledMultipleTimes() {
        Vehicle vehicle = Vehicle.create(1L, "ABC1234", "Toyota", "Corolla",
                2020, "White", VehicleType.CAR);

        vehicle.deactivate();
        vehicle.deactivate();

        assertFalse(vehicle.isActive());
    }
}

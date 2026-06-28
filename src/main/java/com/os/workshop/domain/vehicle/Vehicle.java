package com.os.workshop.domain.vehicle;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Vehicle {

    private Long id;
    private Long clientId;
    private LicensePlate plate;
    private String brand;
    private String model;
    private int year;
    private String color;
    private VehicleType type;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Vehicle() {}

    public static Vehicle create(Long clientId, String rawPlate, String brand,
                                  String model, int year, String color, VehicleType type) {
        Objects.requireNonNull(clientId, "Client ID is required");
        Objects.requireNonNull(type, "Vehicle type is required");
        validateMandatoryFields(brand, model, year);

        var vehicle = new Vehicle();
        vehicle.clientId = clientId;
        vehicle.plate = new LicensePlate(rawPlate);
        vehicle.brand = brand.strip().toUpperCase();
        vehicle.model = model.strip().toUpperCase();
        vehicle.year = year;
        vehicle.color = color.strip().toUpperCase();
        vehicle.type = type;
        vehicle.active = true;
        return vehicle;
    }

    public static Vehicle reconstitute(Long id, Long clientId, String plate,
                                        String brand, String model, int year,
                                        String color, VehicleType type, boolean active,
                                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        var vehicle = new Vehicle();
        vehicle.id = id;
        vehicle.clientId = clientId;
        vehicle.plate = new LicensePlate(plate);
        vehicle.brand = brand;
        vehicle.model = model;
        vehicle.year = year;
        vehicle.color = color;
        vehicle.type = type;
        vehicle.active = active;
        vehicle.createdAt = createdAt;
        vehicle.updatedAt = updatedAt;
        return vehicle;
    }

    public void update(String rawPlate, String brand, String model,
                       int year, String color, VehicleType type) {
        Objects.requireNonNull(type, "Vehicle type is required");
        validateMandatoryFields(brand, model, year);
        this.plate = new LicensePlate(rawPlate);
        this.brand = brand.strip();
        this.model = model.strip();
        this.year = year;
        this.color = color;
        this.type = type;
    }

    public void deactivate() {
        this.active = false;
    }

    private static void validateMandatoryFields(String brand, String model, int year) {
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("Brand is required");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Model is required");
        }
        if (year < 1886) {
            throw new IllegalArgumentException("Invalid vehicle year: " + year + " (minimum: 1886)");
        }
    }
}

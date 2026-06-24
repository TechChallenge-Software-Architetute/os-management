package com.os.workshop.features.vehicle.shared.exception;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(String identifier) {
        super("Vehicle not found: " + identifier);
    }
}

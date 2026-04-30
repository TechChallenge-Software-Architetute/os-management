package com.os.features.vehicle.exception;

/**
 * Lançada quando um veículo não é encontrado pelo identificador informado.
 * Mapeada para HTTP 404 pelo {@code GlobalExceptionHandler}.
 */
public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(String identifier) {
        super("Vehicle not found: " + identifier);
    }
}

package com.os.workshop.features.vehicle.create;

import com.os.workshop.features.vehicle.shared.domain.Vehicle;
import com.os.workshop.features.vehicle.shared.domain.VehicleType;

import java.time.LocalDateTime;

public record CreateVehicleResponse(
        Long id,
        Long clientId,
        String plate,
        String brand,
        String model,
        int year,
        String color,
        VehicleType type,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CreateVehicleResponse from(Vehicle vehicle) {
        return new CreateVehicleResponse(
                vehicle.getId(),
                vehicle.getClientId(),
                vehicle.getPlate().formatted(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getColor(),
                vehicle.getType(),
                vehicle.isActive(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}

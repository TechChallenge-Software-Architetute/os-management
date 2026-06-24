package com.os.workshop.features.vehicle.update;

import com.os.workshop.features.vehicle.shared.domain.VehicleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateVehicleRequest(
        @NotBlank String plate,
        @NotBlank String brand,
        @NotBlank String model,
        @Min(1886) int year,
        String color,
        @NotNull VehicleType type
) {}

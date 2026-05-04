package com.os.workshop.features.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.vehicle.domain.Vehicle;
import com.os.workshop.features.vehicle.domain.VehicleType;
import com.os.workshop.features.vehicle.domain.valueobject.LicensePlate;

import java.time.LocalDateTime;

/**
 * Payload de saída com os dados de um veículo.
 *
 * <p>A placa é retornada no formato com hífen (ex: {@code "ABC-1234"}) via
 * {@link LicensePlate#formatted()}.
 * O factory method {@link #from(Vehicle)} isola a conversão do domínio para o contrato da API.
 *
 * @param id        identificador único do veículo
 * @param clientId  ID do cliente proprietário
 * @param plate     placa formatada (ex: {@code "ABC-1234"} ou {@code "ABC-1D23"})
 * @param brand     marca do veículo
 * @param model     modelo do veículo
 * @param year      ano de fabricação
 * @param color     cor do veículo
 * @param type      tipo do veículo
 * @param active    indica se o veículo está ativo
 * @param createdAt data/hora de criação do registro
 * @param updatedAt data/hora da última atualização
 */
@Schema(description = "Vehicle response payload.")
public record VehicleResponse(
        @Schema(description = "Identifier.", example = "1") Long id,
        @Schema(description = "Client identifier.", example = "1") Long clientId,
        @Schema(description = "Plate.", example = "ABC-1234") String plate,
        @Schema(description = "Brand.", example = "Toyota") String brand,
        @Schema(description = "Model.", example = "Corolla") String model,
        @Schema(description = "Year.", example = "2020") int year,
        @Schema(description = "Color.", example = "Silver") String color,
        @Schema(description = "Type.", example = "CAR") VehicleType type,
        @Schema(description = "Active.", example = "true") boolean active,
        @Schema(description = "Created At.", example = "2026-05-03T10:00:00") LocalDateTime createdAt,
        @Schema(description = "Updated At.", example = "2026-05-03T10:00:00") LocalDateTime updatedAt
) {

    /**
     * Converte um {@link Vehicle} do domínio para o payload de resposta da API.
     *
     * @param vehicle aggregate root do domínio
     * @return payload pronto para serialização JSON
     */
    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
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

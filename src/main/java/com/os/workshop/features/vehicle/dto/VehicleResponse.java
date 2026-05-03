package com.os.workshop.features.vehicle.dto;

import com.os.workshop.features.vehicle.domain.Vehicle;
import com.os.workshop.features.vehicle.domain.VehicleType;
import com.os.workshop.features.vehicle.domain.valueobject.LicensePlate;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Vehicle data returned by the API.")
public record VehicleResponse(
        @Schema(description = "Vehicle unique identifier.", example = "1")
        Long id,

        @Schema(description = "Owner client identifier.", example = "1")
        Long clientId,

        @Schema(description = "Formatted vehicle license plate.", example = "ABC-1234")
        String plate,

        @Schema(description = "Vehicle brand.", example = "TOYOTA")
        String brand,

        @Schema(description = "Vehicle model.", example = "COROLLA")
        String model,

        @Schema(description = "Vehicle manufacturing year.", example = "2022")
        int year,

        @Schema(description = "Vehicle color.", example = "SILVER")
        String color,

        @Schema(description = "Vehicle type.", example = "CAR")
        VehicleType type,

        @Schema(description = "Whether the vehicle is active.", example = "true")
        boolean active,

        @Schema(description = "Record creation date and time.", example = "2026-05-03T12:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Last update date and time.", example = "2026-05-03T12:45:00")
        LocalDateTime updatedAt
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

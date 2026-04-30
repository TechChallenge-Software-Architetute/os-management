package com.os.features.vehicle.dto;

import com.os.features.vehicle.domain.Vehicle;
import com.os.features.vehicle.domain.VehicleType;

import java.time.LocalDateTime;

/**
 * Payload de saída com os dados de um veículo.
 *
 * <p>A placa é retornada no formato com hífen (ex: {@code "ABC-1234"}) via
 * {@link com.os.features.vehicle.domain.valueobject.LicensePlate#formatted()}.
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
public record VehicleResponse(
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

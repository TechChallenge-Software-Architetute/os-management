package com.os.workshop.vehicle.dto;

import com.os.workshop.utils.annotations.UpperCase;
import com.os.workshop.vehicle.domain.VehicleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Payload de entrada para criação e atualização de veículos.
 *
 * <p>Validações aplicadas pelo Bean Validation antes de chegar ao serviço:
 * <ul>
 *   <li>{@code clientId} — obrigatório e positivo; o serviço valida se o cliente existe no sistema</li>
 *   <li>{@code plate} — obrigatório; validação de formato (antigo/Mercosul) feita pelo value object {@code LicensePlate}</li>
 *   <li>{@code brand} e {@code model} — obrigatórios</li>
 *   <li>{@code year} — mínimo 1886 (ano da invenção do automóvel)</li>
 *   <li>{@code type} — obrigatório; deve ser um dos valores de {@link VehicleType}</li>
 * </ul>
 *
 * @param clientId ID do cliente proprietário do veículo
 * @param plate    placa no formato antigo (ex: {@code ABC-1234}) ou Mercosul (ex: {@code ABC1D23})
 * @param brand    marca do veículo (ex: "Toyota")
 * @param model    modelo do veículo (ex: "Corolla")
 * @param year     ano de fabricação
 * @param color    cor do veículo (opcional)
 * @param type     tipo do veículo — um dos valores de {@link VehicleType}
 */
public record VehicleRequest(
        @NotNull @Positive Long clientId,
        @NotBlank @UpperCase String plate,
        @NotBlank @UpperCase String brand,
        @NotBlank @UpperCase String model,
        @Min(1886) int year,
        String color,
        @NotNull VehicleType type
) {}

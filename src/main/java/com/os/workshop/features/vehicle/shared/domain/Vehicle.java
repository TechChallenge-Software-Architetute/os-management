package com.os.workshop.features.vehicle.shared.domain;

import com.os.workshop.features.vehicle.shared.domain.valueobject.LicensePlate;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate root que representa um veículo pertencente a um cliente da oficina.
 *
 * <p>Regras de negócio encapsuladas:
 * <ul>
 *   <li>Todo veículo pertence a exatamente um cliente (referenciado pelo {@code clientId}).</li>
 *   <li>A placa é validada pelo value object {@link LicensePlate} (formato antigo ou Mercosul).</li>
 *   <li>Marca e modelo são obrigatórios.</li>
 *   <li>O ano mínimo é 1886 (ano da invenção do automóvel).</li>
 * </ul>
 *
 * <p>O relacionamento com o cliente é mantido por referência de ID ({@code clientId})
 * em vez de uma referência ao aggregate {@code Client}, seguindo o princípio de isolamento
 * entre aggregates no DDD.
 *
 * <p>Sem dependências externas: nenhuma anotação de framework (Spring, JPA).
 */
@Getter
public class Vehicle {

    private Long id;

    /** Referência ao cliente proprietário por ID — não carrega o aggregate completo. */
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

    /** Construtor protegido — use os factory methods. */
    protected Vehicle() {}

    /**
     * Cria um novo veículo aplicando todas as regras de negócio de criação.
     * A existência do {@code clientId} como cliente válido deve ser verificada
     * pelo serviço de aplicação antes de chamar este método.
     *
     * @param clientId  ID do cliente proprietário (obrigatório)
     * @param rawPlate  placa em qualquer formato aceito — validada pelo {@link LicensePlate}
     * @param brand     marca do veículo (obrigatório)
     * @param model     modelo do veículo (obrigatório)
     * @param year      ano de fabricação (mínimo 1886)
     * @param color     cor do veículo (opcional)
     * @param type      tipo do veículo (obrigatório)
     * @return novo {@code Vehicle} com {@code active = true} e sem ID
     * @throws IllegalArgumentException se algum campo obrigatório for inválido
     */
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

    /**
     * Reconstitui um {@code Vehicle} a partir do estado persistido.
     *
     * <p>Não reaplica regras de criação — assume que os dados foram validados
     * quando o veículo foi originalmente cadastrado.
     * Utilizado exclusivamente pela camada de infraestrutura (mapper de persistência).
     */
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

    /**
     * Atualiza os dados do veículo.
     * A placa pode ser corrigida caso tenha sido cadastrada incorretamente.
     * O cliente proprietário não pode ser alterado por este método.
     *
     * @param rawPlate nova placa (validada pelo {@link LicensePlate})
     * @param brand    nova marca (obrigatório)
     * @param model    novo modelo (obrigatório)
     * @param year     novo ano (mínimo 1886)
     * @param color    nova cor (opcional)
     * @param type     novo tipo (obrigatório)
     * @throws IllegalArgumentException se algum campo obrigatório for inválido
     */
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

    /**
     * Desativa o veículo (soft delete).
     * O registro permanece no banco mas é excluído das listagens ativas.
     */
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

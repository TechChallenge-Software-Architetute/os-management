package com.os.workshop.features.vehicle.domain;

/**
 * Tipos de veículos aceitos na oficina.
 *
 * <p>Usado tanto no domínio quanto na entidade JPA para evitar duplicação,
 * seguindo a mesma abordagem já adotada nos enums {@code ProductType} e {@code UnitOfMeasure}.
 */
public enum VehicleType {

    /** Automóvel de passeio. */
    CAR,

    /** Motocicleta. */
    MOTORCYCLE,

    /** Caminhão ou veículo de carga pesada. */
    TRUCK,

    /** Van ou furgão. */
    VAN
}

package com.os.workshop.features.vehicle.persistence.entity;

import com.os.workshop.features.client.shared.repository.ClientEntity;
import com.os.workshop.features.vehicle.domain.VehicleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade JPA que representa a tabela {@code vehicles} no banco de dados.
 *
 * <p>O relacionamento {@code @ManyToOne} com {@link ClientEntity} materializa a regra de negócio
 * de que tod0 veículo pertence a exatamente um cliente. O carregamento é {@code LAZY} para
 * evitar joins desnecessários nas consultas de listagem.
 *
 * <p>A placa é armazenada sem hífen e em maiúsculas para garantir consistência nas consultas
 * (a formatação é responsabilidade do value object {@code LicensePlate#formatted()}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vehicles")
public class VehicleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /** Relacionamento com o cliente proprietário — carregamento lazy para eficiência. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    /** Placa armazenada sem hífen, em maiúsculas (ex: {@code "ABC1234"}). */
    @Column(nullable = false, unique = true, length = 7)
    private String plate;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private int year;

    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

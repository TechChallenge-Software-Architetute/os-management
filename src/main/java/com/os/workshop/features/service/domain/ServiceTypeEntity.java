package com.os.workshop.features.service.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "service_type")
@Schema(description = "Catalog entry that describes an available service type.")
public class ServiceTypeEntity {

    @Schema(description = "Service type unique identifier.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Schema(description = "Service type name.", example = "OIL_CHANGE")
    @Column(nullable = false, unique = true)
    private String name;

    @Schema(description = "Service type description.", example = "Engine oil replacement and basic inspection.")
    @Column
    private String description;

}

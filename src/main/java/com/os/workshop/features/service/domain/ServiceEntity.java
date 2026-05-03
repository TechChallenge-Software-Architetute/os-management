package com.os.workshop.features.service.domain;

import com.os.workshop.features.service.domain.enums.ServiceStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Service")
@Schema(description = "Workshop service linked to a service order.")
public class ServiceEntity {

    @Schema(description = "Service unique identifier.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Schema(description = "Service type name.", example = "OIL_CHANGE")
    @Column(name = "service_type_name", nullable = false)
    private String serviceTypeName;

    @Schema(description = "Service order identifier associated with the service.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601")
    @Column(name = "id_os", nullable = false)
    private UUID idOS;

    @Schema(description = "Status history for the service.")
    @Column(name = "service_status", nullable = false)
    @Convert(converter = ServiceStatusConverter.class)
    private List<Status> serviceStatus = List.of(
            new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now())
    );
}

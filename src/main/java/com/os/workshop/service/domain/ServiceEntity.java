package com.os.workshop.service.domain;

import com.os.workshop.service.domain.enums.ServiceStatusEnum;
import com.os.workshop.serviceorder.domain.ListToJsonConverter;
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
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "service_type_name", nullable = false)
    private String serviceTypeName;

    @Column(name = "id_os", nullable = false)
    private UUID idOS;

    @Column(name = "service_status", nullable = false)
    @Convert(converter = ServiceStatusConverter.class)
    private List<Status> serviceStatus = List.of(
            new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now())
    );
}

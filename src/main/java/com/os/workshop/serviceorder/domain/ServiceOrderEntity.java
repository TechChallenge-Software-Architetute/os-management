package com.os.workshop.serviceorder.domain;

import com.os.workshop.service.domain.enums.ServiceStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "service_order")
public class ServiceOrderEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "service_type_name", nullable = false)
    private String serviceTypeName;

    @Column(name = "service_status", nullable = false)
    private String serviceStatus = ServiceStatusEnum.TO_DO.getStatus();

    @Column(name = "list_service", nullable = false)
    @Convert(converter = ListToJsonConverter.class)
    private List<String> listService;

}

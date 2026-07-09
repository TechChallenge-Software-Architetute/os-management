package com.os.workshop.domain.serviceorder;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOrder {
    private UUID id;
    private String serviceTypeName;
    private String serviceStatus;
    private List<String> listService;
    private String cpfCnpj;
    private String placaVeiculo;
}

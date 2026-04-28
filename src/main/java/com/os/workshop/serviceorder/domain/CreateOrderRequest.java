package com.os.workshop.serviceorder.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateOrderRequest {
    private String cpfCnpj;
    private String placaVeiculo;
    private List<String> serviceTypes;
}

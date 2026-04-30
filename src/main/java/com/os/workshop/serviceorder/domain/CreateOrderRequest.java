package com.os.workshop.serviceorder.domain;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private String cpfCnpj;
    private String placaVeiculo;
    private List<String> serviceTypes;
}

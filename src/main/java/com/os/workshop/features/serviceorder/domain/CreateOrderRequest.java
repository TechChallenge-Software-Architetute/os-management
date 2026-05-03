package com.os.workshop.features.serviceorder.domain;

import com.os.workshop.features.utils.annotations.UpperCase;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private String cpfCnpj;

    @UpperCase
    private String placaVeiculo;

    @UpperCase
    private List<String> serviceTypes;
}

package com.os.workshop.adapter.in.web.serviceorder;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private String cpfCnpj;
    private String placaVeiculo;
    private List<String> serviceTypes;
}

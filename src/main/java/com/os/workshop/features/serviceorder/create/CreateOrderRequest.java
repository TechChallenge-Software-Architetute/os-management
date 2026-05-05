package com.os.workshop.features.serviceorder.create;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.utils.annotations.UpperCase;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Create Order request payload.")
public class CreateOrderRequest {
    @Schema(description = "Client CPF or CNPJ.", example = "52998224725")
    private String cpfCnpj;

    @UpperCase
    @Schema(description = "Vehicle plate.", example = "ABC-1234")
    private String placaVeiculo;

    @UpperCase
    @Schema(description = "Service types registered in the system.", example = "[\"TROCA_OLEO\", \"ALINHAMENTO\"]")
    private List<String> serviceTypes;
}

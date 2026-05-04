package com.os.workshop.features.serviceorder.create;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.utils.annotations.UpperCase;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Create Order request payload.")
public class CreateOrderRequest {
    @Schema(description = "CPF Cnpj.", example = "529.982.247-25")
    private String cpfCnpj;

    @UpperCase
    @Schema(description = "Placa Veiculo.", example = "ABC-1234")
    private String placaVeiculo;

    @UpperCase
    @Schema(description = "Service Types.", example = "TROCA_OLEO")
    private List<String> serviceTypes;
}

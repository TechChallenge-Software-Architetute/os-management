package com.os.workshop.features.serviceorder.create;

import com.os.workshop.features.utils.annotations.UpperCase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request payload used to create a service order.")
public class CreateOrderRequest {
    @Schema(description = "Customer CPF or CNPJ.", example = "123.456.789-09")
    @NotBlank
    private String cpfCnpj;

    @Schema(description = "Vehicle license plate.", example = "ABC-1234")
    @NotBlank
    @UpperCase
    private String placaVeiculo;

    @Schema(description = "Service types requested for the order.", example = "[\"OIL_CHANGE\", \"ALIGNMENT\"]")
    @NotEmpty
    @UpperCase
    private List<String> serviceTypes;
}

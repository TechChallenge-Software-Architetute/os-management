package com.os.workshop.features.service.create;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.utils.annotations.UpperCase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Create Service request payload.")
public class CreateServiceRequest {
    @UpperCase
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_\\- ]+$", message = "serviceType deve conter apenas letras, números, hífens, underscores e espaços")
    @Schema(description = "Service Type.", example = "BRAKE_SERVICE")
    private String ServiceType;

    @NotNull
    @Schema(description = "Identifier OS.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID idOS;
}

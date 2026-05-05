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
    @Schema(description = "Service type registered in the system.", example = "TROCA_OLEO")
    private String ServiceType;

    @NotNull
    @Schema(description = "Service order identifier.", example = "b46ac51b-5ca6-439b-ba52-a36bd52e8648")
    private UUID idOS;
}

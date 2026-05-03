package com.os.workshop.features.service.domain.requests;

import com.os.workshop.features.utils.annotations.UpperCase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Request payload used to create a service for a service order.")
public class CreateServiceRequest {
    @Schema(description = "Service type name.", example = "OIL_CHANGE")
    @NotBlank
    @UpperCase
    private String ServiceType;

    @Schema(description = "Service order identifier associated with the service.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601")
    @NotNull
    private UUID idOS;
}

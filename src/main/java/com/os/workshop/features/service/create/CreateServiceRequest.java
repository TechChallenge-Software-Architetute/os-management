package com.os.workshop.features.service.create;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.utils.annotations.UpperCase;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Create Service request payload.")
public class CreateServiceRequest {
    @UpperCase
    @Schema(description = "Service Type.", example = "example")
    private String ServiceType;

    @Schema(description = "Identifier OS.", example = "example")
    private UUID idOS;
}

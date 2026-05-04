package com.os.workshop.features.service.domain.requests;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.utils.annotations.UpperCase;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Update Service request payload.")
public class UpdateServiceRequest {
    @UpperCase
    @Schema(description = "Service Type.", example = "example")
    private String serviceType;

    @Schema(description = "Identifier OS.", example = "example")
    private UUID idOS;
}

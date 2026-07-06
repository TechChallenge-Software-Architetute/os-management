package com.os.workshop.adapter.in.web.service;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.infrastructure.config.UpperCase;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Update Service request payload.")
public class UpdateServiceRequest {
    @UpperCase
    @Schema(description = "Service type registered in the system.", example = "ALINHAMENTO")
    private String serviceType;

    @Schema(description = "Service order identifier.", example = "b46ac51b-5ca6-439b-ba52-a36bd52e8648")
    private UUID idOS;
}

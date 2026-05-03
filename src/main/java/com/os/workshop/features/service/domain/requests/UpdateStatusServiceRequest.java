package com.os.workshop.features.service.domain.requests;

import com.os.workshop.features.service.domain.enums.ServiceStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Request payload used to update a service status.")
public class UpdateStatusServiceRequest {
    @Schema(description = "New service status.", example = "IN_PROGRESS")
    @NotNull
    private ServiceStatusEnum status;

    @Schema(description = "Service identifier to update.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601")
    @NotNull
    private UUID id;
}

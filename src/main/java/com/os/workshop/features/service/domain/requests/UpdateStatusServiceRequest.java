package com.os.workshop.features.service.domain.requests;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.service.domain.enums.ServiceStatusEnum;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Update Status Service request payload.")
public class UpdateStatusServiceRequest {
    @Schema(description = "Status.", example = "DOING")
    private ServiceStatusEnum status;
    @Schema(description = "Identifier.", example = "1")
    private UUID id;
}

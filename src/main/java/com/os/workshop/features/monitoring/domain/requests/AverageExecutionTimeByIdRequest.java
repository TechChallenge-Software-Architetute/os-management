package com.os.workshop.features.monitoring.domain.requests;

import com.os.workshop.features.monitoring.domain.enums.AverageTimeEnum;
import com.os.workshop.features.utils.annotations.UpperCase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Request payload used to calculate average execution time for one service.")
public class AverageExecutionTimeByIdRequest {
    @Schema(description = "Time unit used to calculate the average execution time.", example = "HOURS")
    @NotNull
    @UpperCase
    AverageTimeEnum timeUnit;

    @Schema(description = "Service identifier.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601")
    @NotNull
    UUID id;
}

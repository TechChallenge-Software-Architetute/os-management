package com.os.workshop.features.monitoring.averageExecutionTimeById;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.monitoring.shared.domain.AverageTimeEnum;
import com.os.workshop.features.utils.annotations.UpperCase;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Get Average Execution Time By Id request payload.")
public class GetAverageExecutionTimeByIdRequest {
    @UpperCase
    @Schema(description = "Time Unit.", example = "SECONDS")
    AverageTimeEnum timeUnit;

    @Schema(description = "Identifier.", example = "1")
    UUID id;
}

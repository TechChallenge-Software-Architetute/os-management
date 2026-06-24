package com.os.workshop.features.monitoring.averageExecutionTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.monitoring.shared.domain.AverageTimeEnum;
import com.os.workshop.features.utils.annotations.UpperCase;
import lombok.Data;

@Data
@Schema(description = "Get Average Execution Time request payload.")
public class GetAverageExecutionTimeRequest {
    @UpperCase
    @Schema(description = "Time Unit.", example = "SECONDS")
    AverageTimeEnum timeUnit;
}

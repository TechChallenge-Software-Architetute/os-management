package com.os.workshop.features.monitoring.domain.requests;

import com.os.workshop.features.monitoring.domain.enums.AverageTimeEnum;
import com.os.workshop.features.utils.annotations.UpperCase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request payload used to calculate average execution time for all service types.")
public class AverageExecutionTimeRequest {
    @Schema(description = "Time unit used to calculate the average execution time.", example = "HOURS")
    @NotNull
    @UpperCase
    AverageTimeEnum timeUnit;
}

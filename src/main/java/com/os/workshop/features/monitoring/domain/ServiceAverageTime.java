package com.os.workshop.features.monitoring.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Average execution time calculated for a service type or service.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAverageTime {
    @Schema(description = "Service type name.", example = "OIL_CHANGE")
    private String serviceTypeName;

    @Schema(description = "Average execution time represented in seconds.", example = "5400.0")
    private double averageTimeSeconds;
}

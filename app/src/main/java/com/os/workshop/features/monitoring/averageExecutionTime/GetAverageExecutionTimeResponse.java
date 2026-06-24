package com.os.workshop.features.monitoring.averageExecutionTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.monitoring.shared.domain.ServiceAverageTime;

import java.util.List;

@Schema(description = "Get Average Execution Time response payload.")
public record GetAverageExecutionTimeResponse(List<ServiceAverageTime> averages) {
    public static GetAverageExecutionTimeResponse from(List<ServiceAverageTime> averages) {
        return new GetAverageExecutionTimeResponse(averages);
    }
}

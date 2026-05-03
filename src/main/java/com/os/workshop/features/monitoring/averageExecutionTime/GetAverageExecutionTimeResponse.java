package com.os.workshop.features.monitoring.averageExecutionTime;

import com.os.workshop.features.monitoring.shared.domain.ServiceAverageTime;

import java.util.List;

public record GetAverageExecutionTimeResponse(List<ServiceAverageTime> averages) {
    public static GetAverageExecutionTimeResponse from(List<ServiceAverageTime> averages) {
        return new GetAverageExecutionTimeResponse(averages);
    }
}

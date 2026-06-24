package com.os.workshop.features.monitoring.averageExecutionTimeById;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.monitoring.shared.domain.ServiceAverageTime;

@Schema(description = "Get Average Execution Time By Id response payload.")
public record GetAverageExecutionTimeByIdResponse(String serviceTypeName, double averageTime) {
    public static GetAverageExecutionTimeByIdResponse from(ServiceAverageTime averageTime) {
        return new GetAverageExecutionTimeByIdResponse(averageTime.getServiceTypeName(), averageTime.getAverageTime());
    }
}

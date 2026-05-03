package com.os.workshop.features.monitoring.averageExecutionTimeById;

import com.os.workshop.features.monitoring.shared.domain.ServiceAverageTime;

public record GetAverageExecutionTimeByIdResponse(String serviceTypeName, double averageTime) {
    public static GetAverageExecutionTimeByIdResponse from(ServiceAverageTime averageTime) {
        return new GetAverageExecutionTimeByIdResponse(averageTime.getServiceTypeName(), averageTime.getAverageTime());
    }
}

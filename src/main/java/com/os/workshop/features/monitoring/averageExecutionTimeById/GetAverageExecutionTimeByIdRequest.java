package com.os.workshop.features.monitoring.averageExecutionTimeById;

import com.os.workshop.features.monitoring.shared.domain.AverageTimeEnum;
import com.os.workshop.features.utils.annotations.UpperCase;
import lombok.Data;

import java.util.UUID;

@Data
public class GetAverageExecutionTimeByIdRequest {
    @UpperCase
    AverageTimeEnum timeUnit;

    UUID id;
}

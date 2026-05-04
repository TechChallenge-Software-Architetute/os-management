package com.os.workshop.features.monitoring.averageExecutionTime;

import com.os.workshop.features.monitoring.shared.domain.AverageTimeEnum;
import com.os.workshop.features.utils.annotations.UpperCase;
import lombok.Data;

@Data
public class GetAverageExecutionTimeRequest {
    @UpperCase
    AverageTimeEnum timeUnit;
}

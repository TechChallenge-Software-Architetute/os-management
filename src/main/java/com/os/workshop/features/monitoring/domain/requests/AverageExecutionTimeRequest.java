package com.os.workshop.features.monitoring.domain.requests;

import com.os.workshop.features.monitoring.domain.enums.AverageTimeEnum;
import com.os.workshop.features.utils.annotations.UpperCase;
import lombok.Data;

@Data
public class AverageExecutionTimeRequest {
    @UpperCase
    AverageTimeEnum timeUnit;
}

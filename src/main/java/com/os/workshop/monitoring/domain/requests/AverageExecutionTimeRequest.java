package com.os.workshop.monitoring.domain.requests;

import com.os.workshop.monitoring.domain.enums.AverageTimeEnum;
import com.os.workshop.utils.annotations.UpperCase;
import lombok.Data;

import java.util.UUID;

@Data
public class AverageExecutionTimeRequest {
    @UpperCase
    AverageTimeEnum timeUnit;
}

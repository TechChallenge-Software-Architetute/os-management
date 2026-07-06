package com.os.workshop.adapter.in.web.monitoring;

import com.os.workshop.domain.monitoring.AverageTimeEnum;
import lombok.Data;

@Data
public class GetAverageExecutionTimeRequest {
    AverageTimeEnum timeUnit;
}

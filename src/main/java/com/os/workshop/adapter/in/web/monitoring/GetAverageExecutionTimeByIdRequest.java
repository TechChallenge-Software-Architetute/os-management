package com.os.workshop.adapter.in.web.monitoring;

import com.os.workshop.domain.monitoring.AverageTimeEnum;
import lombok.Data;
import java.util.UUID;

@Data
public class GetAverageExecutionTimeByIdRequest {
    AverageTimeEnum timeUnit;
    UUID id;
}

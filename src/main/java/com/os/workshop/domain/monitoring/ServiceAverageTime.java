package com.os.workshop.domain.monitoring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAverageTime {
    private String serviceTypeName;
    private double averageTime;
}

package com.os.workshop.monitoring.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAverageTime {
    private String serviceTypeName;
    private double averageTimeSeconds;
}

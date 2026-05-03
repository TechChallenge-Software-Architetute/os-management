package com.os.workshop.features.monitoring.shared.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AverageTimeEnumTest {

    @Test
    void calculatesDurationInConfiguredUnit() {
        Duration duration = Duration.ofSeconds(120);

        assertEquals(120.0, AverageTimeEnum.SECONDS.calculate(duration));
        assertEquals(2.0, AverageTimeEnum.MINUTES.calculate(duration));
        assertEquals(120.0 / 3600.0, AverageTimeEnum.HOURS.calculate(duration));
    }
}

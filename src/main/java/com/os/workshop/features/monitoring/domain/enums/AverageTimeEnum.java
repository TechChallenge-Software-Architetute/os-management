package com.os.workshop.features.monitoring.domain.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AverageTimeEnum {

    SECONDS("SECONDS") {
        @Override
        public double calculate(Duration duration) {
            return duration.toSeconds();
        }
    },

    MINUTES("MINUTES") {
        @Override
        public double calculate(Duration duration) {
            return duration.toMillis() / 60000.0;
        }
    },

    HOURS("HOURS") {
        @Override
        public double calculate(Duration duration) {
            return duration.toMillis() / 3600000.0;
        }
    };

    private final String time;

    public abstract double calculate(Duration duration);
}


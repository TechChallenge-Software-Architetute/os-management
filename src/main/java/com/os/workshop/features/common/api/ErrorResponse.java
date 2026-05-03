package com.os.workshop.features.common.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Standard error response returned by the API.")
public record ErrorResponse(
        @Schema(description = "Human-readable error message.", example = "Validation failed")
        String message,

        @Schema(description = "HTTP status code.", example = "400")
        int status,

        @Schema(description = "Date and time when the error occurred.", example = "2026-05-03T12:30:00")
        LocalDateTime timestamp,

        @Schema(description = "Optional field-level validation details.", example = "[\"email: must be a well-formed email address\"]")
        List<String> errors
) {
    public ErrorResponse(String message, int status, LocalDateTime timestamp) {
        this(message, status, timestamp, List.of());
    }
}

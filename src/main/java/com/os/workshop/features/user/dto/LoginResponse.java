package com.os.workshop.features.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login response payload.")
public record LoginResponse(
        @Schema(description = "Token.", example = "eyJhbGciOiJIUzI1NiJ9") String token,
        @Schema(description = "Type.", example = "CAR") String type,
        @Schema(description = "Expires In.", example = "86400000") long expiresIn
) {}

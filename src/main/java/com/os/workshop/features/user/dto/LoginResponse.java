package com.os.workshop.features.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication token returned after a successful login.")
public record LoginResponse(
        @Schema(description = "JWT access token.", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token,

        @Schema(description = "Token type.", example = "Bearer")
        String type,

        @Schema(description = "Token expiration time in seconds.", example = "3600")
        long expiresIn
) {}

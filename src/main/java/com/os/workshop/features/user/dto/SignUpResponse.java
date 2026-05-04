package com.os.workshop.features.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(description = "Sign Up response payload.")
public record SignUpResponse(
        @Schema(description = "Identifier.", example = "1") UUID id,
        @Schema(description = "Email.", example = "user@example.com") String email,
        @Schema(description = "Roles.", example = "ROLE_USER") Set<String> roles
) {}

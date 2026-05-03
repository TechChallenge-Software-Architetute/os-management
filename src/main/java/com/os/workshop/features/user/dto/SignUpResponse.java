package com.os.workshop.features.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(description = "User account data returned after sign-up.")
public record SignUpResponse(
        @Schema(description = "User unique identifier.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601")
        UUID id,

        @Schema(description = "User email used as login identifier.", example = "mechanic@example.com")
        String email,

        @Schema(description = "Roles assigned to the user.", example = "[\"ADMIN\"]")
        Set<String> roles
) {}

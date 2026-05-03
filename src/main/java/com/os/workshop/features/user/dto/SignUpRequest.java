package com.os.workshop.features.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

@Schema(description = "Request payload used to create a user account.")
public record SignUpRequest (
    @Schema(description = "User email used as login identifier.", example = "mechanic@example.com")
    @Email
    @NotBlank
    String email,

    @Schema(description = "Initial user password.", example = "Str0ngP@ssword")
    @NotBlank
    String password,

    @Schema(description = "Roles assigned to the user.", example = "[\"ADMIN\"]")
    @NotEmpty
    Set<String> roles
) {}

package com.os.workshop.features.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

@Schema(description = "Sign Up request payload.")
public record SignUpRequest (
    @Email
    @NotBlank
    @Schema(description = "Email.", example = "user@example.com") String email,

    @NotBlank
    @Schema(description = "Password.", example = "password123") String password,

    @Schema(description = "Roles.", example = "ROLE_USER") Set<String> roles
) {}

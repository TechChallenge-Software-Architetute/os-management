package com.os.workshop.features.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request payload.")
public record LoginRequest(

        @Email(message = "Invalid email")
        @NotBlank(message = "Email is a mandatory field")
        @Schema(description = "Email.", example = "user@example.com") String email,

        @NotBlank(message = "Password is a mandatory field")
        @Schema(description = "Password.", example = "password123") String password

) {}

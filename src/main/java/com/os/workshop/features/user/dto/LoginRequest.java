package com.os.workshop.features.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Authentication request payload.")
public record LoginRequest(

        @Schema(description = "User email used as login identifier.", example = "admin@example.com")
        @Email(message = "Invalid email")
        @NotBlank(message = "Email is a mandatory field")
        String email,

        @Schema(description = "User password.", example = "Str0ngP@ssword")
        @NotBlank(message = "Password is a mandatory field")
        String password

) {}

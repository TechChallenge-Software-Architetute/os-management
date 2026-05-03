package com.os.workshop.features.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record SignUpRequest (
    @Email
    @NotBlank
    String email,

    @NotBlank
    String password,

    Set<String> roles
) {}

package com.os.workshop.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Email(message = "Invalid email")
        @NotBlank(message = "Email is a mandatory field")
        String email,

        @NotBlank(message = "Password is a mandatory field")
        String password

) {}
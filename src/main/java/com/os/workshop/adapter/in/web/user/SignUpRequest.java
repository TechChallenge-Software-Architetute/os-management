package com.os.workshop.adapter.in.web.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record SignUpRequest(
        @Email @NotBlank String email,
        @NotBlank(message = "CPF is a mandatory field") String cpf,
        @NotBlank String password,
        Set<String> roles
) {}

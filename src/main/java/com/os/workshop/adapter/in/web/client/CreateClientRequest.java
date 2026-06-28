package com.os.workshop.adapter.in.web.client;

import com.os.workshop.infrastructure.config.UpperCase;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateClientRequest(
        @NotBlank @UpperCase String name,
        @NotBlank String cpf,
        @Email String email,
        String phone
) {}

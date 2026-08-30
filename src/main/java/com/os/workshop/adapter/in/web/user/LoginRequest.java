package com.os.workshop.adapter.in.web.user;

import jakarta.validation.constraints.NotBlank;

/**
 * {@code login} aceita tanto email quanto CPF — o {@code LoginUseCase}
 * detecta o formato e resolve para o usuário correspondente.
 */
public record LoginRequest(
        @NotBlank(message = "Login (email or CPF) is a mandatory field") String login,
        @NotBlank(message = "Password is a mandatory field") String password
) {}

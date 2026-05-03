package com.os.workshop.features.client.create;

import com.os.workshop.features.utils.annotations.UpperCase;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload de entrada para criação de clientes.
 *
 * @param name  nome completo do cliente
 * @param cpf   CPF com ou sem formatação (ex: "123.456.789-09" ou "12345678909")
 * @param email e-mail de contato (opcional)
 * @param phone telefone de contato (opcional)
 */
public record CreateClientRequest(
        @NotBlank @UpperCase String name,
        @NotBlank String cpf,
        @Email String email,
        String phone
) {}

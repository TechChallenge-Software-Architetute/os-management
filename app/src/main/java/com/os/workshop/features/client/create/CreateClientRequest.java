package com.os.workshop.features.client.create;

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Create Client request payload.")
public record CreateClientRequest(
        @Schema(description = "Name.", example = "John Doe") @NotBlank @UpperCase String name,
        @Schema(description = "CPF.", example = "52998224725") @NotBlank String cpf,
        @Schema(description = "Email.", example = "user@example.com") @Email String email,
        @Schema(description = "Phone.", example = "11999999999") String phone
) {}

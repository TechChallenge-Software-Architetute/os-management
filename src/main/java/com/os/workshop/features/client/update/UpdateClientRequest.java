package com.os.workshop.features.client.update;

import com.os.workshop.features.utils.annotations.UpperCase;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload de entrada para atualização de clientes.
 *
 * @param name  nome completo do cliente
 * @param cpf   CPF com ou sem formatação
 * @param email e-mail de contato (opcional)
 * @param phone telefone de contato (opcional)
 */
public record UpdateClientRequest(
        @NotBlank @UpperCase String name,
        @NotBlank String cpf,
        @Email String email,
        String phone
) {}

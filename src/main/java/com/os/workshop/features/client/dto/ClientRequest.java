package com.os.workshop.features.client.dto;

import com.os.workshop.features.utils.annotations.UpperCase;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload de entrada para criação e atualização de clientes.
 *
 * <p>Validações aplicadas pelo Bean Validation antes de chegar ao serviço:
 * <ul>
 *   <li>{@code name} — obrigatório e não pode ser vazio</li>
 *   <li>{@code cpf} — obrigatório (validação de dígitos é feita no value object {@code Cpf})</li>
 *   <li>{@code email} — deve ser um endereço de e-mail válido quando informado</li>
 * </ul>
 *
 * @param name  nome completo do cliente
 * @param cpf   CPF com ou sem formatação (ex: "123.456.789-09" ou "12345678909")
 * @param email e-mail de contato (opcional)
 * @param phone telefone de contato (opcional)
 */
public record ClientRequest(
        @NotBlank @UpperCase String name,
        @NotBlank String cpf,
        @Email String email,
        String phone
) {}

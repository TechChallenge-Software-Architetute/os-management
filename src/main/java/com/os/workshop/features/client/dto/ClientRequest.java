package com.os.workshop.features.client.dto;

import com.os.workshop.features.utils.annotations.UpperCase;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request payload used to create or update a client.")
public record ClientRequest(
        @Schema(description = "Client full name.", example = "MARIA SILVA")
        @NotBlank @UpperCase String name,

        @Schema(description = "Brazilian CPF with or without punctuation.", example = "123.456.789-09")
        @NotBlank String cpf,

        @Schema(description = "Client contact email.", example = "maria.silva@example.com")
        @Email String email,

        @Schema(description = "Client contact phone number.", example = "+55 11 99999-0000")
        String phone
) {}

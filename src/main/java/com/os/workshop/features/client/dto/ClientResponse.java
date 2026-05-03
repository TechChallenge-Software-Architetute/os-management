package com.os.workshop.features.client.dto;

import com.os.workshop.features.client.domain.Client;
import com.os.workshop.features.client.domain.valueobject.Cpf;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Payload de saída com os dados de um cliente.
 *
 * <p>O CPF é retornado no formato oficial (XXX.XXX.XXX-XX) via {@link Cpf#formatted()}.
 * O factory method {@link #from(Client)} isola a conversão do domínio para o contrato da API.
 *
 * @param id        identificador único do cliente
 * @param name      nome completo
 * @param cpf       CPF formatado (XXX.XXX.XXX-XX)
 * @param email     e-mail de contato
 * @param phone     telefone de contato
 * @param active    indica se o cliente está ativo
 * @param createdAt data/hora de criação do registro
 * @param updatedAt data/hora da última atualização
 */
@Schema(description = "Client data returned by the API.")
public record ClientResponse(
        @Schema(description = "Client unique identifier.", example = "1")
        Long id,

        @Schema(description = "Client full name.", example = "MARIA SILVA")
        String name,

        @Schema(description = "Formatted Brazilian CPF.", example = "123.456.789-09")
        String cpf,

        @Schema(description = "Client contact email.", example = "maria.silva@example.com")
        String email,

        @Schema(description = "Client contact phone number.", example = "+55 11 99999-0000")
        String phone,

        @Schema(description = "Whether the client is active.", example = "true")
        boolean active,

        @Schema(description = "Record creation date and time.", example = "2026-05-03T12:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Last update date and time.", example = "2026-05-03T12:45:00")
        LocalDateTime updatedAt
) {

    /**
     * Converte um {@link Client} do domínio para o payload de resposta da API.
     *
     * @param client aggregate root do domínio
     * @return payload pronto para serialização JSON
     */
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getCpf().formatted(),
                client.getEmail(),
                client.getPhone(),
                client.isActive(),
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }
}

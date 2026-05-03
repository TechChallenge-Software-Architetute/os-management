package com.os.workshop.features.client.dto;

import com.os.workshop.features.client.domain.Client;
import com.os.workshop.features.client.domain.valueobject.Cpf;

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
public record ClientResponse(
        Long id,
        String name,
        String cpf,
        String email,
        String phone,
        boolean active,
        LocalDateTime createdAt,
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

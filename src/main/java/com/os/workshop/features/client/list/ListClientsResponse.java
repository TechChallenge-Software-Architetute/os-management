package com.os.workshop.features.client.list;

import com.os.workshop.features.client.shared.domain.Client;

import java.time.LocalDateTime;

/**
 * Payload de saída para listagem de clientes.
 */
public record ListClientsResponse(
        Long id,
        String name,
        String cpf,
        String email,
        String phone,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ListClientsResponse from(Client client) {
        return new ListClientsResponse(
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

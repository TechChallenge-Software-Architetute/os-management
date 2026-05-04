package com.os.workshop.features.client.create;

import com.os.workshop.features.client.shared.domain.Client;

import java.time.LocalDateTime;

/**
 * Payload de saída após criação de um cliente.
 */
public record CreateClientResponse(
        Long id,
        String name,
        String cpf,
        String email,
        String phone,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CreateClientResponse from(Client client) {
        return new CreateClientResponse(
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

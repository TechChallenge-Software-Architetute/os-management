package com.os.workshop.features.client.findById;

import com.os.workshop.features.client.shared.domain.Client;

import java.time.LocalDateTime;

/**
 * Payload de saída para busca de cliente por ID.
 */
public record FindClientByIdResponse(
        Long id,
        String name,
        String cpf,
        String email,
        String phone,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FindClientByIdResponse from(Client client) {
        return new FindClientByIdResponse(
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

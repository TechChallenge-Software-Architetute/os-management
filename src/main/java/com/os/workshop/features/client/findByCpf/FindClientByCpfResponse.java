package com.os.workshop.features.client.findByCpf;

import com.os.workshop.features.client.shared.domain.Client;

import java.time.LocalDateTime;

/**
 * Payload de saída para busca de cliente por CPF.
 */
public record FindClientByCpfResponse(
        Long id,
        String name,
        String cpf,
        String email,
        String phone,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FindClientByCpfResponse from(Client client) {
        return new FindClientByCpfResponse(
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

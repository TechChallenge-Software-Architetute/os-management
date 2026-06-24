package com.os.workshop.features.client.findByCpf;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.client.shared.domain.Client;

import java.time.LocalDateTime;

/**
 * Payload de saída para busca de cliente por CPF.
 */
@Schema(description = "Find Client By Cpf response payload.")
public record FindClientByCpfResponse(
        @Schema(description = "Identifier.", example = "1") Long id,
        @Schema(description = "Name.", example = "John Doe") String name,
        @Schema(description = "CPF.", example = "52998224725") String cpf,
        @Schema(description = "Email.", example = "user@example.com") String email,
        @Schema(description = "Phone.", example = "11999999999") String phone,
        @Schema(description = "Active.", example = "true") boolean active,
        @Schema(description = "Created At.", example = "2026-05-03T10:00:00") LocalDateTime createdAt,
        @Schema(description = "Updated At.", example = "2026-05-03T10:00:00") LocalDateTime updatedAt
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

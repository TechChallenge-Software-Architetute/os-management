package com.os.workshop.adapter.in.web.client;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.domain.client.Client;

import java.time.LocalDateTime;

@Schema(description = "List Clients response payload.")
public record ClientResponse(
        @Schema(description = "Identifier.", example = "1") Long id,
        @Schema(description = "Name.", example = "John Doe") String name,
        @Schema(description = "Document (CPF or CNPJ).", example = "52998224725") String document,
        @Schema(description = "Email.", example = "user@example.com") String email,
        @Schema(description = "Phone.", example = "11999999999") String phone,
        @Schema(description = "Active.", example = "true") boolean active,
        @Schema(description = "Created At.", example = "2026-05-03T10:00:00") LocalDateTime createdAt,
        @Schema(description = "Updated At.", example = "2026-05-03T10:00:00") LocalDateTime updatedAt
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getDocument().getValue(),
                client.getEmail(),
                client.getPhone(),
                client.isActive(),
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }
}

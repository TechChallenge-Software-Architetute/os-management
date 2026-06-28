package com.os.workshop.domain.client;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate root que representa um cliente da oficina.
 */
@Getter
public class Client {

    private Long id;
    private String name;
    private Cpf cpf;
    private String email;
    private String phone;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Client() {}

    public static Client create(String name, String rawCpf, String email, String phone) {
        Objects.requireNonNull(name, "Name is required");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        var client = new Client();
        client.name = name.strip().toUpperCase();
        client.cpf = new Cpf(rawCpf);
        client.email = email;
        client.phone = phone;
        client.active = true;
        return client;
    }

    public static Client reconstitute(Long id, String name, String cpfValue,
                                       String email, String phone, boolean active,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        var client = new Client();
        client.id = id;
        client.name = name;
        client.cpf = new Cpf(cpfValue);
        client.email = email;
        client.phone = phone;
        client.active = active;
        client.createdAt = createdAt;
        client.updatedAt = updatedAt;
        return client;
    }

    public void update(String name, String email, String phone) {
        Objects.requireNonNull(name, "Name is required");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        this.name = name.strip();
        this.email = email;
        this.phone = phone;
    }

    public void deactivate() {
        this.active = false;
    }

    void setId(Long id) {
        this.id = id;
    }
}

package com.os.workshop.domain.client;

import com.os.workshop.domain.shared.Cpf;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Client {

    private Long id;
    private String name;
    private Document document;
    private String email;
    private String phone;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Client() {}

    public static Client create(String name, String rawDocument, String email, String phone) {
        Objects.requireNonNull(name, "Name is required");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        var client = new Client();
        client.name = name.strip().toUpperCase();
        client.document = Document.of(rawDocument);
        client.email = email;
        client.phone = phone;
        client.active = true;
        return client;
    }

    public static Client reconstitute(Long id, String name, String documentValue,
                                       String email, String phone, boolean active,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        var client = new Client();
        client.id = id;
        client.name = name;
        client.document = Document.of(documentValue);
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

    @Deprecated
    public Cpf getCpf() {
        return new Cpf(document.getValue());
    }
}

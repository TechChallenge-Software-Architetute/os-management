package com.os.workshop.client.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade JPA que representa a tabela {@code clients} no banco de dados.
 *
 * <p>O CPF é armazenado sem formatação (apenas 11 dígitos) para garantir
 * consistência nas consultas. A formatação é responsabilidade do domínio ({@code Cpf#formatted()}).
 *
 * <p>Os timestamps {@code createdAt} e {@code updatedAt} são gerenciados automaticamente
 * pelos callbacks {@link PrePersist} e {@link PreUpdate}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clients")
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** CPF armazenado sem formatação (11 dígitos). */
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    private String email;

    private String phone;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

package com.os.workshop.features.client.domain;

import com.os.workshop.features.client.domain.valueobject.Cpf;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate root que representa um cliente da oficina.
 *
 * <p>Segue os princípios de DDD:
 * <ul>
 *   <li><b>Sem dependências externas</b> — nenhuma anotação de framework (Spring, JPA).</li>
 *   <li><b>Invariantes garantidas</b> — nome obrigatório; CPF validado pelo value object {@link Cpf}.</li>
 *   <li><b>Mutação controlada</b> — apenas através dos métodos {@link #update} e {@link #deactivate}.</li>
 *   <li><b>Factory methods</b> — {@link #create} para novos clientes; {@link #reconstitute} para
 *       reconstrução a partir da persistência sem reaplicar regras de criação.</li>
 * </ul>
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

    /** Construtor protegido — use os factory methods. */
    protected Client() {}

    /**
     * Cria um novo cliente aplicando todas as regras de negócio de criação.
     *
     * @param name     nome completo (obrigatório)
     * @param rawCpf   CPF com ou sem formatação — validado pelo {@link Cpf} value object
     * @param email    e-mail de contato (opcional)
     * @param phone    telefone de contato (opcional)
     * @return novo {@code Client} com {@code active = true} e sem ID (gerado na persistência)
     * @throws IllegalArgumentException se o nome for nulo/em branco ou o CPF for inválido
     */
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

    /**
     * Reconstitui um {@code Client} a partir do estado persistido.
     *
     * <p>Diferente de {@link #create}, este método <b>não</b> reaplica regras de negócio de criação —
     * assume que os dados já foram validados quando o registro foi originalmente criado.
     * Utilizado exclusivamente pela camada de infraestrutura (mapper de persistência).
     */
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

    /**
     * Atualiza os dados de contato do cliente.
     * O CPF não pode ser alterado após o cadastro (é o identificador único de negócio).
     *
     * @param name  novo nome (obrigatório)
     * @param email novo e-mail (opcional)
     * @param phone novo telefone (opcional)
     * @throws IllegalArgumentException se o nome for nulo/em branco
     */
    public void update(String name, String email, String phone) {
        Objects.requireNonNull(name, "Name is required");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        this.name = name.strip();
        this.email = email;
        this.phone = phone;
    }

    /**
     * Desativa o cliente (soft delete).
     * O registro permanece no banco mas é excluído das listagens ativas.
     */
    public void deactivate() {
        this.active = false;
    }

    /** Setter de pacote usado pelo mapper para definir o ID após a persistência. */
    void setId(Long id) {
        this.id = id;
    }
}

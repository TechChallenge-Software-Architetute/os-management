package com.os.workshop.client;

import com.os.workshop.client.dto.ClientRequest;
import com.os.workshop.client.dto.ClientResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST para operações CRUD de clientes.
 *
 * <p>Endpoints disponíveis:
 * <ul>
 *   <li>{@code POST   /api/clients}           — cadastra um novo cliente</li>
 *   <li>{@code GET    /api/clients}            — lista todos os clientes ativos</li>
 *   <li>{@code GET    /api/clients/{id}}       — busca cliente por ID</li>
 *   <li>{@code GET    /api/clients/cpf/{cpf}}  — busca cliente por CPF</li>
 *   <li>{@code PUT    /api/clients/{id}}       — atualiza dados de contato do cliente</li>
 *   <li>{@code DELETE /api/clients/{id}}       — desativa o cliente (soft delete)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    /**
     * Cadastra um novo cliente.
     * O CPF deve ser único no sistema.
     *
     * @param request dados do cliente
     * @return cliente criado com HTTP 201
     */
    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ClientResponse.from(clientService.create(request)));
    }

    /**
     * Lista todos os clientes ativos cadastrados no sistema.
     *
     * @return lista de clientes com HTTP 200
     */
    @GetMapping
    public ResponseEntity<List<ClientResponse>> findAll() {
        List<ClientResponse> response = clientService.findAll().stream()
                .map(ClientResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Busca um cliente pelo seu identificador único.
     *
     * @param id ID do cliente
     * @return dados do cliente com HTTP 200, ou 404 se não encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ClientResponse.from(clientService.findById(id)));
    }

    /**
     * Busca um cliente pelo CPF.
     * Aceita CPF com ou sem formatação no path (ex: {@code 12345678909} ou {@code 123.456.789-09}).
     *
     * @param cpf CPF do cliente
     * @return dados do cliente com HTTP 200, ou 404 se não encontrado
     */
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<ClientResponse> findByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(ClientResponse.from(clientService.findByCpf(cpf)));
    }

    /**
     * Atualiza os dados de contato de um cliente existente.
     * O CPF não pode ser alterado.
     *
     * @param id      ID do cliente
     * @param request novos dados do cliente
     * @return cliente atualizado com HTTP 200, ou 404 se não encontrado
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(ClientResponse.from(clientService.update(id, request)));
    }

    /**
     * Desativa um cliente (soft delete).
     * O cliente permanece no banco mas não aparece mais nas listagens ativas.
     *
     * @param id ID do cliente
     * @return HTTP 204 em caso de sucesso, ou 404 se não encontrado
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        clientService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}

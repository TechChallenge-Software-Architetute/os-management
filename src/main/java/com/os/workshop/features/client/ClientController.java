package com.os.workshop.features.client;

import com.os.workshop.features.common.api.ErrorResponse;
import com.os.workshop.features.client.dto.ClientRequest;
import com.os.workshop.features.client.dto.ClientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@Tag(name = "Clients", description = "Manage workshop customers and their contact information.")
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    private final ClientService clientService;

    /**
     * Cadastra um novo cliente.
     * O CPF deve ser único no sistema.
     *
     * @param request dados do cliente
     * @return cliente criado com HTTP 201
     */
    @PostMapping
    @Operation(summary = "Create client", description = "Creates a new active client. The CPF must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client created successfully", content = @Content(schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or duplicated CPF", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClientResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Client data to create.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ClientRequest.class)))
            @Valid @RequestBody ClientRequest request) {
        logger.info("Creating client. cpf={}", request.cpf());
        var response = ClientResponse.from(clientService.create(request));
        logger.info("Client created. id={}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lista todos os clientes ativos cadastrados no sistema.
     *
     * @return lista de clientes com HTTP 200
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List clients", description = "Lists all active clients registered in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clients listed successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClientResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ClientResponse>> findAll() {
        logger.info("Listing active clients.");
        List<ClientResponse> response = clientService.findAll().stream()
                .map(ClientResponse::from)
                .toList();
        logger.info("Clients listed. count={}", response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Busca um cliente pelo seu identificador único.
     *
     * @param id ID do cliente
     * @return dados do cliente com HTTP 200, ou 404 se não encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Find client by ID", description = "Retrieves an active client by its unique identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client found", content = @Content(schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid client identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClientResponse> findById(
            @Parameter(description = "Client unique identifier.", example = "1", required = true)
            @PathVariable Long id) {
        logger.info("Finding client by id. id={}", id);
        var response = ClientResponse.from(clientService.findById(id));
        logger.info("Client found. id={}", response.id());
        return ResponseEntity.ok(response);
    }

    /**
     * Busca um cliente pelo CPF.
     * Aceita CPF com ou sem formatação no path (ex: {@code 12345678909} ou {@code 123.456.789-09}).
     *
     * @param cpf CPF do cliente
     * @return dados do cliente com HTTP 200, ou 404 se não encontrado
     */
    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Find client by CPF", description = "Retrieves an active client by CPF. CPF can be sent with or without punctuation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client found", content = @Content(schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid CPF", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClientResponse> findByCpf(
            @Parameter(description = "Brazilian CPF with or without punctuation.", example = "123.456.789-09", required = true)
            @PathVariable String cpf) {
        logger.info("Finding client by CPF. cpf={}", cpf);
        var response = ClientResponse.from(clientService.findByCpf(cpf));
        logger.info("Client found by CPF. id={}", response.id());
        return ResponseEntity.ok(response);
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
    @Operation(summary = "Update client", description = "Updates contact data for an existing client. The CPF cannot be changed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client updated successfully", content = @Content(schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClientResponse> update(
            @Parameter(description = "Client unique identifier.", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Client data to update.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ClientRequest.class)))
            @Valid @RequestBody ClientRequest request) {
        logger.info("Updating client. id={}", id);
        var response = ClientResponse.from(clientService.update(id, request));
        logger.info("Client updated. id={}", response.id());
        return ResponseEntity.ok(response);
    }

    /**
     * Desativa um cliente (soft delete).
     * O cliente permanece no banco mas não aparece mais nas listagens ativas.
     *
     * @param id ID do cliente
     * @return HTTP 204 em caso de sucesso, ou 404 se não encontrado
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate client", description = "Soft-deletes a client so it no longer appears in active listings.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Client deactivated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid client identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "Client unique identifier.", example = "1", required = true)
            @PathVariable Long id) {
        logger.info("Deactivating client. id={}", id);
        clientService.deactivate(id);
        logger.info("Client deactivated. id={}", id);
        return ResponseEntity.noContent().build();
    }
}

package com.os.workshop.features.client;

import com.os.workshop.features.client.dto.ClientOrderDetailResponse;
import com.os.workshop.features.client.dto.ClientOrderSummaryResponse;
import com.os.workshop.features.client.dto.ClientRequest;
import com.os.workshop.features.client.dto.ClientResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    @PreAuthorize("ROLE_ADMIN")
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

    // ==================== Client Portal Endpoints ====================

    /**
     * Lists all service orders for the logged-in client.
     * The client is identified from the JWT token (email).
     * Returns basic information: order ID, vehicle plate, status, and included services.
     *
     * @param userDetails the authenticated user (injected from JWT)
     * @return list of order summaries
     */
    @GetMapping("/my-orders")
    public ResponseEntity<List<ClientOrderSummaryResponse>> findMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        var orders = clientService.findMyOrders(userDetails.getUsername()).stream()
                .map(ClientOrderSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(orders);
    }

    /**
     * Returns detailed information about a single service order for the logged-in client.
     * Includes the budget with item-level price breakdown when available.
     * Validates that the order belongs to the authenticated client.
     *
     * @param userDetails the authenticated user (injected from JWT)
     * @param orderId     the service order UUID
     * @return order details with budget
     */
    @GetMapping("/my-orders/{orderId}")
    public ResponseEntity<ClientOrderDetailResponse> findMyOrderDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID orderId) {
        var order = clientService.findMyOrderById(userDetails.getUsername(), orderId);
        var budget = clientService.findBudgetForOrder(orderId);
        return ResponseEntity.ok(ClientOrderDetailResponse.from(order, budget));
    }

    /**
     * Approves a service order on behalf of the logged-in client.
     * The order must be in AGUARDANDO_APROVACAO status.
     * Changes the order status to APROVADO.
     *
     * @param userDetails the authenticated user (injected from JWT)
     * @param orderId     the service order UUID to approve
     * @return the updated order detail with budget
     */
    @PatchMapping("/my-orders/{orderId}/approve")
    public ResponseEntity<ClientOrderDetailResponse> approveMyOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID orderId) {
        var order = clientService.approveMyOrder(userDetails.getUsername(), orderId);
        var budget = clientService.findBudgetForOrder(orderId);
        return ResponseEntity.ok(ClientOrderDetailResponse.from(order, budget));
    }
}

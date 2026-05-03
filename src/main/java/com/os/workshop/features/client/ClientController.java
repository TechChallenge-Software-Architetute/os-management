package com.os.workshop.features.client;

import com.os.workshop.features.client.approveOrder.ApproveMyOrderHandler;
import com.os.workshop.features.client.approveOrder.ApproveMyOrderResponse;
import com.os.workshop.features.client.create.CreateClientHandler;
import com.os.workshop.features.client.create.CreateClientRequest;
import com.os.workshop.features.client.create.CreateClientResponse;
import com.os.workshop.features.client.deactivate.DeactivateClientHandler;
import com.os.workshop.features.client.findByCpf.FindClientByCpfHandler;
import com.os.workshop.features.client.findByCpf.FindClientByCpfResponse;
import com.os.workshop.features.client.findById.FindClientByIdHandler;
import com.os.workshop.features.client.findById.FindClientByIdResponse;
import com.os.workshop.features.client.list.ListClientsHandler;
import com.os.workshop.features.client.list.ListClientsResponse;
import com.os.workshop.features.client.myOrderDetail.FindMyOrderDetailHandler;
import com.os.workshop.features.client.myOrderDetail.FindMyOrderDetailResponse;
import com.os.workshop.features.client.myOrders.FindMyOrdersHandler;
import com.os.workshop.features.client.myOrders.FindMyOrdersResponse;
import com.os.workshop.features.client.update.UpdateClientHandler;
import com.os.workshop.features.client.update.UpdateClientRequest;
import com.os.workshop.features.client.update.UpdateClientResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 *   <li>{@code POST   /api/clients}                          — cadastra um novo cliente</li>
 *   <li>{@code GET    /api/clients}                           — lista todos os clientes ativos</li>
 *   <li>{@code GET    /api/clients/{id}}                      — busca cliente por ID</li>
 *   <li>{@code GET    /api/clients/cpf/{cpf}}                 — busca cliente por CPF</li>
 *   <li>{@code PUT    /api/clients/{id}}                      — atualiza dados de contato do cliente</li>
 *   <li>{@code DELETE /api/clients/{id}}                      — desativa o cliente (soft delete)</li>
 *   <li>{@code GET    /api/clients/my-orders}                 — lista ordens do cliente logado</li>
 *   <li>{@code GET    /api/clients/my-orders/{orderId}}       — detalhe de uma ordem do cliente logado</li>
 *   <li>{@code PATCH  /api/clients/my-orders/{orderId}/approve} — aprova uma ordem do cliente logado</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final CreateClientHandler createClientHandler;
    private final ListClientsHandler listClientsHandler;
    private final FindClientByIdHandler findClientByIdHandler;
    private final FindClientByCpfHandler findClientByCpfHandler;
    private final UpdateClientHandler updateClientHandler;
    private final DeactivateClientHandler deactivateClientHandler;
    private final FindMyOrdersHandler findMyOrdersHandler;
    private final FindMyOrderDetailHandler findMyOrderDetailHandler;
    private final ApproveMyOrderHandler approveMyOrderHandler;

    @PostMapping
    public ResponseEntity<CreateClientResponse> create(@Valid @RequestBody CreateClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateClientResponse.from(createClientHandler.handle(request)));
    }

    @GetMapping
    public ResponseEntity<List<ListClientsResponse>> findAll() {
        List<ListClientsResponse> response = listClientsHandler.handle().stream()
                .map(ListClientsResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FindClientByIdResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(FindClientByIdResponse.from(findClientByIdHandler.handle(id)));
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<FindClientByCpfResponse> findByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(FindClientByCpfResponse.from(findClientByCpfHandler.handle(cpf)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateClientResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody UpdateClientRequest request) {
        return ResponseEntity.ok(UpdateClientResponse.from(updateClientHandler.handle(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateClientHandler.handle(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Client Portal Endpoints ====================

    @GetMapping("/my-orders")
    public ResponseEntity<List<FindMyOrdersResponse>> findMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        var orders = findMyOrdersHandler.handle(userDetails.getUsername()).stream()
                .map(FindMyOrdersResponse::from)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/my-orders/{orderId}")
    public ResponseEntity<FindMyOrderDetailResponse> findMyOrderDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID orderId) {
        var result = findMyOrderDetailHandler.handle(userDetails.getUsername(), orderId);
        return ResponseEntity.ok(FindMyOrderDetailResponse.from(result.order(), result.budget()));
    }

    @PatchMapping("/my-orders/{orderId}/approve")
    public ResponseEntity<ApproveMyOrderResponse> approveMyOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID orderId) {
        var result = approveMyOrderHandler.handle(userDetails.getUsername(), orderId);
        return ResponseEntity.ok(ApproveMyOrderResponse.from(result.order(), result.budget()));
    }
}

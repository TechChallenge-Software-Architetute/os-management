package com.os.workshop.adapter.in.web.client;

import com.os.workshop.application.client.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final CreateClientUseCase createClientUseCase;
    private final ListClientsUseCase listClientsUseCase;
    private final FindClientByIdUseCase findClientByIdUseCase;
    private final FindClientByCpfUseCase findClientByCpfUseCase;
    private final UpdateClientUseCase updateClientUseCase;
    private final DeactivateClientUseCase deactivateClientUseCase;
    private final FindMyOrdersUseCase findMyOrdersUseCase;
    private final FindMyOrderDetailUseCase findMyOrderDetailUseCase;
    private final ApproveMyOrderUseCase approveMyOrderUseCase;

    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody CreateClientRequest request) {
        var client = createClientUseCase.execute(request.name(), request.cpf(), request.email(), request.phone());
        return ResponseEntity.status(HttpStatus.CREATED).body(ClientResponse.from(client));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> findAll() {
        var response = listClientsUseCase.execute().stream().map(ClientResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ClientResponse.from(findClientByIdUseCase.execute(id)));
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<ClientResponse> findByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(ClientResponse.from(findClientByCpfUseCase.execute(cpf)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateClientRequest request) {
        var client = updateClientUseCase.execute(id, request.name(), request.email(), request.phone());
        return ResponseEntity.ok(ClientResponse.from(client));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateClientUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<FindMyOrdersResponse>> findMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        var orders = findMyOrdersUseCase.execute(userDetails.getUsername()).stream()
                .map(FindMyOrdersResponse::from).toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/my-orders/{orderId}")
    public ResponseEntity<FindMyOrderDetailResponse> findMyOrderDetail(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID orderId) {
        var result = findMyOrderDetailUseCase.execute(userDetails.getUsername(), orderId);
        return ResponseEntity.ok(FindMyOrderDetailResponse.from(result.order(), result.budget()));
    }

    @PatchMapping("/my-orders/{orderId}/approve")
    public ResponseEntity<ApproveMyOrderResponse> approveMyOrder(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID orderId) {
        var result = approveMyOrderUseCase.execute(userDetails.getUsername(), orderId);
        return ResponseEntity.ok(ApproveMyOrderResponse.from(result.order(), result.budget()));
    }
}

package com.os.workshop.adapter.in.web.client;

import com.os.workshop.application.client.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Clients", description = "Gestao de Clientes e Decisao de Orcamento")
public class ClientController {

    private final CreateClientUseCase createClientUseCase;
    private final ListClientsUseCase listClientsUseCase;
    private final FindClientByIdUseCase findClientByIdUseCase;
    private final FindClientByCpfUseCase findClientByCpfUseCase;
    private final UpdateClientUseCase updateClientUseCase;
    private final DeactivateClientUseCase deactivateClientUseCase;
    private final FindMyOrdersUseCase findMyOrdersUseCase;
    private final FindMyOrderDetailUseCase findMyOrderDetailUseCase;
    private final DecideOrderUseCase decideOrderUseCase;

    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody CreateClientRequest request) {
        var client = createClientUseCase.execute(request.name(), request.document(), request.email(), request.phone());
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
        var orders = (isClient(userDetails)
                ? findMyOrdersUseCase.executeByDocument(userDetails.getUsername())
                : findMyOrdersUseCase.execute(userDetails.getUsername())).stream()
                .map(FindMyOrdersResponse::from).toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/my-orders/{orderId}")
    public ResponseEntity<FindMyOrderDetailResponse> findMyOrderDetail(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID orderId) {
        var result = isClient(userDetails)
                ? findMyOrderDetailUseCase.executeByDocument(userDetails.getUsername(), orderId)
                : findMyOrderDetailUseCase.execute(userDetails.getUsername(), orderId);
        return ResponseEntity.ok(FindMyOrderDetailResponse.from(result.order(), result.budget()));
    }

    @PostMapping("/my-orders/{orderId}/decision")
    @Operation(summary = "Aprovar ou recusar orcamento", description = "O cliente autenticado decide aprovar ou recusar o orcamento da OS. Retorna 204 No Content.")
    public ResponseEntity<Void> decideOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID orderId,
            @Valid @RequestBody DecisionRequest request) {
        if (isClient(userDetails)) {
            decideOrderUseCase.executeByDocument(userDetails.getUsername(), orderId, request.decision(), request.reason());
        } else {
            decideOrderUseCase.execute(userDetails.getUsername(), orderId, request.decision(), request.reason());
        }
        return ResponseEntity.noContent().build();
    }

    // Client (CPF) tokens authenticate with ROLE_CLIENT; their username is the CPF.
    private boolean isClient(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CLIENT".equals(a.getAuthority()));
    }
}

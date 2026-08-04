package com.os.workshop.adapter.in.web.serviceorder;

import com.os.workshop.application.serviceorder.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "Service Orders", description = "Gestao de Ordens de Servico")
public class ServiceOrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final UpdateOrderUseCase updateOrderUseCase;
    private final FindOrderByIdUseCase findOrderByIdUseCase;
    private final ListOrdersUseCase listOrdersUseCase;
    private final FindOrdersByDocumentUseCase findOrdersByDocumentUseCase;

    @PostMapping
    @Operation(summary = "Criar ordem de servico", description = "Cria uma nova OS associando cliente (CPF/CNPJ), veiculo (placa) e tipos de servico.")
    public ResponseEntity<ServiceOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        var order = createOrderUseCase.execute(request.getCpfCnpj(), request.getPlacaVeiculo(), request.getServiceTypes());
        return ResponseEntity.status(HttpStatus.CREATED).body(ServiceOrderResponse.from(order));
    }

    @GetMapping
    @Operation(summary = "Listar OS ativas", description = "Retorna OS ativas (exclui FINALIZADA/ENTREGUE), ordenadas por prioridade de status e data de criacao.")
    public ResponseEntity<List<ServiceOrderResponse>> listOrders() {
        var responses = listOrdersUseCase.execute().stream().map(ServiceOrderResponse::from).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Buscar OS por ID", description = "Retorna uma OS pelo seu UUID.")
    public ResponseEntity<ServiceOrderResponse> findOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(ServiceOrderResponse.from(findOrderByIdUseCase.execute(id)));
    }

    @GetMapping("/document/{cpfCnpj}")
    @Operation(summary = "Buscar OS por documento", description = "Retorna todas as OS vinculadas a um cliente pelo CPF ou CNPJ (historico completo).")
    public ResponseEntity<List<ServiceOrderResponse>> findByDocument(@PathVariable String cpfCnpj) {
        var responses = findOrdersByDocumentUseCase.execute(cpfCnpj).stream()
                .map(ServiceOrderResponse::from).toList();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar status da OS", description = "Atualiza o status da OS. Valida transicoes permitidas. Retorna 204 No Content.")
    public ResponseEntity<Void> updateOrder(
            @PathVariable UUID id, @RequestBody UpdateOrderRequest request) {
        updateOrderUseCase.execute(id, request.getStatus());
        return ResponseEntity.noContent().build();
    }
}

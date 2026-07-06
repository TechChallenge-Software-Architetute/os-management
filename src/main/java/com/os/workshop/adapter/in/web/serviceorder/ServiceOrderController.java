package com.os.workshop.adapter.in.web.serviceorder;

import com.os.workshop.application.serviceorder.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class ServiceOrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final UpdateOrderUseCase updateOrderUseCase;
    private final FindOrderByIdUseCase findOrderByIdUseCase;
    private final ListOrdersUseCase listOrdersUseCase;

    @PostMapping
    public ResponseEntity<ServiceOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        var order = createOrderUseCase.execute(request.getCpfCnpj(), request.getPlacaVeiculo(), request.getServiceTypes());
        return ResponseEntity.status(HttpStatus.CREATED).body(ServiceOrderResponse.from(order));
    }

    @GetMapping
    public ResponseEntity<List<ServiceOrderResponse>> listOrders() {
        var responses = listOrdersUseCase.execute().stream().map(ServiceOrderResponse::from).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOrderResponse> findOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(ServiceOrderResponse.from(findOrderByIdUseCase.execute(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ServiceOrderResponse> updateOrder(
            @PathVariable UUID id, @RequestBody UpdateOrderRequest request) {
        try {
            var order = updateOrderUseCase.execute(id, request.getStatus());
            return ResponseEntity.ok(ServiceOrderResponse.from(order));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}

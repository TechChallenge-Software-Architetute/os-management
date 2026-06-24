package com.os.workshop.features.serviceorder;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.media.ExampleObject;

import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.Operation;

import com.os.workshop.features.serviceorder.create.CreateOrderHandler;
import com.os.workshop.features.serviceorder.create.CreateOrderRequest;
import com.os.workshop.features.serviceorder.create.CreateOrderResponse;
import com.os.workshop.features.serviceorder.findById.FindOrderByIdHandler;
import com.os.workshop.features.serviceorder.findById.FindOrderByIdResponse;
import com.os.workshop.features.serviceorder.list.ListOrdersHandler;
import com.os.workshop.features.serviceorder.list.ListOrdersResponse;
import com.os.workshop.features.serviceorder.update.UpdateOrderHandler;
import com.os.workshop.features.serviceorder.update.UpdateOrderRequest;
import com.os.workshop.features.serviceorder.update.UpdateOrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Tag(name = "Service Orders", description = "Manage service orders.")
@RestController
@RequestMapping("/order")
public class ServiceOrderController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceOrderController.class);

    @Autowired
    private CreateOrderHandler createOrderHandler;

    @Autowired
    private UpdateOrderHandler updateOrderHandler;

    @Autowired
    private FindOrderByIdHandler findOrderByIdHandler;

    @Autowired
    private ListOrdersHandler listOrdersHandler;

    @Operation(summary = "Create service order", description = "Create service order endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Request payload for this operation",
        required = true,
        content = @Content(
                schema = @Schema(implementation = CreateOrderRequest.class),
                examples = @ExampleObject(value = "{\"cpfCnpj\":\"52998224725\",\"placaVeiculo\":\"ABC-1234\",\"serviceTypes\":[\"TROCA_OLEO\",\"ALINHAMENTO\"]}")
        )
)
@RequestBody CreateOrderRequest request) {
        try {
            var serviceOrder = createOrderHandler.handle(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(CreateOrderResponse.from(serviceOrder));
        } catch (Exception e) {
            logger.error("Erro ao criar ordem de servico. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "List service orders", description = "List service orders endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<ListOrdersResponse>> listOrders() {
        logger.info("Recebida requisicao para consultar ordens de servico.");

        try {
            var orders = listOrdersHandler.handle();
            var responses = orders.stream()
                    .map(ListOrdersResponse::from)
                    .toList();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Erro ao consultar ordens de servico. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Find service order by id", description = "Find service order by id endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FindOrderByIdResponse> findOrderById(@PathVariable UUID id) {
        logger.info("Recebida requisicao para consultar ordem de servico por ID: {}", id);

        try {
            var order = findOrderByIdHandler.handle(id);
            return ResponseEntity.ok(FindOrderByIdResponse.from(order));
        } catch (RuntimeException e) {
            logger.error("Ordem de servico nao encontrada. ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Erro ao consultar ordem de servico por ID. ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update service order", description = "Update service order endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<UpdateOrderResponse> updateOrder(
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(

                    description = "Request payload for this operation",

                    required = true,

                    content = @Content(
                            schema = @Schema(implementation = UpdateOrderRequest.class),
                            examples = @ExampleObject(value = "{\"status\":\"EM_DIAGNOSTICO\"}")
                    )

            )

            @RequestBody UpdateOrderRequest request
    ) {
        try {
            var serviceOrder = updateOrderHandler.handle(id, request);
            return ResponseEntity.ok(UpdateOrderResponse.from(serviceOrder));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao atualizar ordem de servico. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

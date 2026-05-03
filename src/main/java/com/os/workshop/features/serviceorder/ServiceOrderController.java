package com.os.workshop.features.serviceorder;

import com.os.workshop.features.common.api.ErrorResponse;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping({"/api/service-orders", "/order"})
@Tag(name = "Service Orders", description = "Manage service orders, their requested services, status, and generated budgets.")
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

    @PostMapping
    @Operation(summary = "Create service order", description = "Creates a service order for a customer vehicle and requested service types. The /order path is kept as a backward-compatible alias.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Service order created successfully", content = @Content(schema = @Schema(implementation = CreateOrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Client, vehicle, or service type not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CreateOrderResponse> createOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Service order data to create.", required = true, content = @Content(schema = @Schema(implementation = CreateOrderRequest.class)))
            @Valid @RequestBody CreateOrderRequest request) {
        logger.info("Creating service order. cpfCnpj={}, plate={}", request.getCpfCnpj(), request.getPlacaVeiculo());
        try {
            var serviceOrder = createOrderHandler.handle(request);
            var response = CreateOrderResponse.from(serviceOrder);
            logger.info("Service order created. id={}", response.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Erro ao criar ordem de servico. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    @Operation(summary = "List service orders", description = "Lists all service orders registered in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service orders listed successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ListOrdersResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ListOrdersResponse>> listOrders() {
        logger.info("Recebida requisicao para consultar ordens de servico.");

        try {
            var orders = listOrdersHandler.handle();
            var responses = orders.stream()
                    .map(ListOrdersResponse::from)
                    .toList();
            logger.info("Service orders listed. count={}", responses.size());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Erro ao consultar ordens de servico. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find service order by ID", description = "Retrieves a service order by its unique identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service order found", content = @Content(schema = @Schema(implementation = FindOrderByIdResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid service order identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Service order not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FindOrderByIdResponse> findOrderById(
            @Parameter(description = "Service order unique identifier.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601", required = true)
            @PathVariable UUID id) {
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

    @PatchMapping("/{id}")
    @Operation(summary = "Update service order", description = "Updates the status of an existing service order.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service order updated successfully", content = @Content(schema = @Schema(implementation = UpdateOrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status update request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Service order not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UpdateOrderResponse> updateOrder(
            @Parameter(description = "Service order unique identifier.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601", required = true)
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Service order status update.", required = true, content = @Content(schema = @Schema(implementation = UpdateOrderRequest.class)))
            @Valid @RequestBody UpdateOrderRequest request
    ) {
        logger.info("Updating service order. id={}, status={}", id, request.getStatus());
        try {
            var serviceOrder = updateOrderHandler.handle(id, request);
            var response = UpdateOrderResponse.from(serviceOrder);
            logger.info("Service order updated. id={}", response.id());
            return ResponseEntity.ok(response);
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

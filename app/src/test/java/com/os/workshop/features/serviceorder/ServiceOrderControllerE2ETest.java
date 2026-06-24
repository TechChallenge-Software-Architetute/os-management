package com.os.workshop.features.serviceorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.os.workshop.features.serviceorder.create.CreateOrderHandler;
import com.os.workshop.features.serviceorder.create.CreateOrderRequest;
import com.os.workshop.features.serviceorder.findById.FindOrderByIdHandler;
import com.os.workshop.features.serviceorder.list.ListOrdersHandler;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;
import com.os.workshop.features.serviceorder.shared.domain.enums.OrderServiceStatusEnum;
import com.os.workshop.features.serviceorder.update.UpdateOrderHandler;
import com.os.workshop.features.serviceorder.update.UpdateOrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ServiceOrderControllerE2ETest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CreateOrderHandler createOrderHandler;

    @Mock
    private UpdateOrderHandler updateOrderHandler;

    @Mock
    private FindOrderByIdHandler findOrderByIdHandler;

    @Mock
    private ListOrdersHandler listOrdersHandler;

    @InjectMocks
    private ServiceOrderController serviceOrderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(serviceOrderController).build();
    }

    private ServiceOrder createServiceOrder() {
        return ServiceOrder.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .serviceTypeName("[TROCA_OLEO]")
                .serviceStatus("RECEBIDA")
                .listService(List.of("TROCA_OLEO"))
                .cpfCnpj("12345678900")
                .placaVeiculo("ABC1234")
                .budget(null)
                .build();
    }

    private CreateOrderRequest createOrderRequest() {
        var request = new CreateOrderRequest();
        request.setCpfCnpj("12345678900");
        request.setPlacaVeiculo("ABC1234");
        request.setServiceTypes(List.of("TROCA_OLEO"));
        return request;
    }

    // ==================== POST /order ====================

    @Test
    void whenCreatingOrderWithValidData_thenReturns201() throws Exception {
        ServiceOrder order = createServiceOrder();
        when(createOrderHandler.handle(any(CreateOrderRequest.class))).thenReturn(order);

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.serviceTypeName").value("[TROCA_OLEO]"))
                .andExpect(jsonPath("$.serviceStatus").value("RECEBIDA"))
                .andExpect(jsonPath("$.cpfCnpj").value("12345678900"))
                .andExpect(jsonPath("$.placaVeiculo").value("ABC1234"));
    }

    @Test
    void whenCreatingOrderAndHandlerThrowsException_thenReturns500() throws Exception {
        when(createOrderHandler.handle(any(CreateOrderRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest())))
                .andExpect(status().isInternalServerError());
    }

    // ==================== GET /order ====================

    @Test
    void whenListingOrders_thenReturns200WithList() throws Exception {
        ServiceOrder order = createServiceOrder();
        when(listOrdersHandler.handle()).thenReturn(List.of(order));

        mockMvc.perform(get("/order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$[0].serviceStatus").value("RECEBIDA"));
    }

    @Test
    void whenListingOrdersAndHandlerThrowsException_thenReturns500() throws Exception {
        when(listOrdersHandler.handle()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/order"))
                .andExpect(status().isInternalServerError());
    }

    // ==================== GET /order/{id} ====================

    @Test
    void whenFindingOrderByExistingId_thenReturns200() throws Exception {
        UUID orderId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        ServiceOrder order = createServiceOrder();
        when(findOrderByIdHandler.handle(orderId)).thenReturn(order);

        mockMvc.perform(get("/order/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.serviceTypeName").value("[TROCA_OLEO]"))
                .andExpect(jsonPath("$.cpfCnpj").value("12345678900"));
    }

    @Test
    void whenFindingOrderByNonExistingId_thenReturns404() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(findOrderByIdHandler.handle(orderId))
                .thenThrow(new RuntimeException("Ordem de servico nao encontrada"));

        mockMvc.perform(get("/order/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    // ==================== PATCH /order/{id} ====================

    @Test
    void whenUpdatingOrderWithValidStatus_thenReturns200() throws Exception {
        UUID orderId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        ServiceOrder updatedOrder = createServiceOrder();
        updatedOrder.setServiceStatus("EM_DIAGNOSTICO");

        when(updateOrderHandler.handle(eq(orderId), any(UpdateOrderRequest.class))).thenReturn(updatedOrder);

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setStatus(OrderServiceStatusEnum.EM_DIAGNOSTICO);

        mockMvc.perform(patch("/order/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.serviceStatus").value("EM_DIAGNOSTICO"));
    }

    @Test
    void whenUpdatingOrderAndNotFound_thenReturns404() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(updateOrderHandler.handle(eq(orderId), any(UpdateOrderRequest.class)))
                .thenThrow(new NoSuchElementException("Ordem nao encontrada"));

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setStatus(OrderServiceStatusEnum.EM_DIAGNOSTICO);

        mockMvc.perform(patch("/order/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenUpdatingOrderWithInvalidData_thenReturns400() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(updateOrderHandler.handle(eq(orderId), any(UpdateOrderRequest.class)))
                .thenThrow(new IllegalArgumentException("Status invalido"));

        UpdateOrderRequest request = new UpdateOrderRequest();

        mockMvc.perform(patch("/order/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenUpdatingOrderAndUnexpectedError_thenReturns500() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(updateOrderHandler.handle(eq(orderId), any(UpdateOrderRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setStatus(OrderServiceStatusEnum.FINALIZADA);

        mockMvc.perform(patch("/order/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}

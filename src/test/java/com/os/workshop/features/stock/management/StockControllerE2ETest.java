package com.os.workshop.features.stock.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.os.workshop.features.product.exception.GlobalExceptionHandler;
import com.os.workshop.features.stock.domain.Stock;
import com.os.workshop.features.stock.domain.StockMovement;
import com.os.workshop.features.stock.domain.StockMovementType;
import com.os.workshop.features.stock.repository.StockMovementRepository;
import com.os.workshop.features.stock.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StockControllerE2ETest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @BeforeEach
    void setUp() {
        StockService stockService = new StockService(stockRepository, movementRepository);
        StockController stockController = new StockController(stockService);
        mockMvc = MockMvcBuilders.standaloneSetup(stockController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Stock createStock() {
        Stock stock = new Stock();
        stock.setId(1L);
        stock.setProductId(2L);
        stock.setQuantity(new BigDecimal("100"));
        stock.setReservedQuantity(new BigDecimal("20"));
        stock.setMinimumQuantity(new BigDecimal("10"));
        stock.recalculateAvailableQuantity();
        stock.setCreatedAt(LocalDateTime.now());
        stock.setUpdatedAt(LocalDateTime.now());
        return stock;
    }

    @Test
    void whenCreatingStockWithValidData_thenReturns201() throws Exception {
        Long productId = 3L;
        when(stockRepository.existsByProductId(productId)).thenReturn(false);
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> {
            Stock s = i.getArgument(0);
            s.setId(4L);
            return s;
        });

        StockRequest request = new StockRequest(productId, new BigDecimal("50"), new BigDecimal("5"));

        mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(50));
    }

    @Test
    void whenFindingStockByProductId_thenReturns200WithAvailableQuantity() throws Exception {
        Stock stock = createStock();
        when(stockRepository.findByProductId(stock.getProductId())).thenReturn(Optional.of(stock));

        mockMvc.perform(get("/api/stocks/product/{productId}", stock.getProductId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.reservedQuantity").value(20))
                .andExpect(jsonPath("$.availableQuantity").value(80));
    }

    @Test
    void whenAddingStockEntry_thenReturns200WithIncreasedQuantity() throws Exception {
        Stock stock = createStock();
        when(stockRepository.findByProductId(stock.getProductId())).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        StockMovementRequest request = new StockMovementRequest(new BigDecimal("30"), "Delivery");

        mockMvc.perform(patch("/api/stocks/product/{productId}/entry", stock.getProductId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(130));
    }

    @Test
    void whenFindingLowStock_thenReturns200WithLowStockItems() throws Exception {
        Stock low = createStock();
        low.setQuantity(new BigDecimal("8"));
        low.setReservedQuantity(BigDecimal.ZERO);
        low.recalculateAvailableQuantity();

        when(stockRepository.findAll()).thenReturn(List.of(low));

        mockMvc.perform(get("/api/stocks/low"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void whenFindingMovements_thenReturns200WithMovementHistory() throws Exception {
        Stock stock = createStock();
        when(stockRepository.findByProductId(stock.getProductId())).thenReturn(Optional.of(stock));

        StockMovement movement = new StockMovement();
        movement.setId(5L);
        movement.setStockId(stock.getId());
        movement.setType(StockMovementType.ENTRY);
        movement.setQuantity(new BigDecimal("50"));
        movement.setReason("Initial");
        movement.setCreatedAt(LocalDateTime.now());

        when(movementRepository.findByStockId(stock.getId())).thenReturn(List.of(movement));

        mockMvc.perform(get("/api/stocks/product/{productId}/movements", stock.getProductId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("ENTRY"));
    }
}

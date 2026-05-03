package com.os.workshop.features.product.supply;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.os.workshop.features.product.domain.ProductType;
import com.os.workshop.features.product.domain.Supply;
import com.os.workshop.features.product.domain.UnitOfMeasure;
import com.os.workshop.features.product.exception.GlobalExceptionHandler;
import com.os.workshop.features.product.repository.SupplyRepository;
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
class SupplyControllerE2ETest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SupplyRepository supplyRepository;

    @BeforeEach
    void setUp() {
        SupplyService supplyService = new SupplyService(supplyRepository);
        SupplyController supplyController = new SupplyController(supplyService);
        mockMvc = MockMvcBuilders.standaloneSetup(supplyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Supply createSupply() {
        Supply supply = new Supply();
        supply.setId(1L);
        supply.setName("Engine Oil");
        supply.setSku("OIL-5W30");
        supply.setType(ProductType.SUPPLY);
        supply.setUnit(UnitOfMeasure.LITER);
        supply.setCategory("Lubricants");
        supply.setBrand("Mobil");
        supply.setCostPrice(new BigDecimal("25.00"));
        supply.setSalePrice(new BigDecimal("50.00"));
        supply.setActive(true);
        supply.setFractionalAllowed(true);
        supply.setPackageSize(new BigDecimal("1"));
        supply.setCreatedAt(LocalDateTime.now());
        supply.setUpdatedAt(LocalDateTime.now());
        return supply;
    }

    @Test
    void whenCreatingSupplyWithValidData_thenReturns201() throws Exception {
        when(supplyRepository.existsBySku("OIL-5W30")).thenReturn(false);
        when(supplyRepository.save(any(Supply.class))).thenAnswer(i -> {
            Supply s = i.getArgument(0);
            s.setId(1L);
            return s;
        });

        SupplyRequest request = new SupplyRequest("Engine Oil", "OIL-5W30", UnitOfMeasure.LITER,
                "Lubricants", "Mobil", new BigDecimal("25"), new BigDecimal("50"), true, new BigDecimal("1"));

        mockMvc.perform(post("/api/supplies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Engine Oil"));
    }

    @Test
    void whenFindingAllSupplies_thenReturns200WithList() throws Exception {
        when(supplyRepository.findAllActive()).thenReturn(List.of(createSupply()));

        mockMvc.perform(get("/api/supplies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void whenFindingSupplyByExistingSku_thenReturns200() throws Exception {
        Supply supply = createSupply();
        when(supplyRepository.findBySku("OIL-5W30")).thenReturn(Optional.of(supply));

        mockMvc.perform(get("/api/supplies/sku/{sku}", "OIL-5W30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fractionalAllowed").value(true));
    }

    @Test
    void whenDeactivatingSupply_thenReturns204() throws Exception {
        Supply supply = createSupply();
        when(supplyRepository.findById(1L)).thenReturn(Optional.of(supply));
        when(supplyRepository.save(any(Supply.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(delete("/api/supplies/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    // ==================== GET /api/supplies/{id} ====================

    @Test
    void whenFindingSupplyByExistingId_thenReturns200() throws Exception {
        Supply supply = createSupply();
        when(supplyRepository.findById(1L)).thenReturn(Optional.of(supply));

        mockMvc.perform(get("/api/supplies/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Engine Oil"))
                .andExpect(jsonPath("$.sku").value("OIL-5W30"))
                .andExpect(jsonPath("$.fractionalAllowed").value(true));
    }

    // ==================== PUT /api/supplies/{id} ====================

    @Test
    void whenUpdatingSupplyWithValidData_thenReturns200() throws Exception {
        Supply supply = createSupply();
        when(supplyRepository.findById(1L)).thenReturn(Optional.of(supply));
        when(supplyRepository.existsBySku("OIL-10W40")).thenReturn(false);
        when(supplyRepository.save(any(Supply.class))).thenAnswer(i -> i.getArgument(0));

        SupplyRequest updateRequest = new SupplyRequest("Synthetic Oil", "OIL-10W40", UnitOfMeasure.LITER,
                "Lubricants", "Castrol", new BigDecimal("30"), new BigDecimal("60"), true, new BigDecimal("1"));

        mockMvc.perform(put("/api/supplies/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Synthetic Oil"))
                .andExpect(jsonPath("$.sku").value("OIL-10W40"));
    }
}

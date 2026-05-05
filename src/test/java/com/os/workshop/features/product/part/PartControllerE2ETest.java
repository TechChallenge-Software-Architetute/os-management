package com.os.workshop.features.product.part;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.os.workshop.features.product.part.create.CreatePartHandler;
import com.os.workshop.features.product.part.create.CreatePartRequest;
import com.os.workshop.features.product.part.deactivate.DeactivatePartHandler;
import com.os.workshop.features.product.part.findById.FindPartByIdHandler;
import com.os.workshop.features.product.part.findBySku.FindPartBySkuHandler;
import com.os.workshop.features.product.part.list.ListPartsHandler;
import com.os.workshop.features.product.part.update.UpdatePartHandler;
import com.os.workshop.features.product.part.update.UpdatePartRequest;
import com.os.workshop.features.product.shared.domain.Part;
import com.os.workshop.features.product.shared.domain.ProductType;
import com.os.workshop.features.product.shared.domain.UnitOfMeasure;
import com.os.workshop.features.product.shared.exception.GlobalExceptionHandler;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PartControllerE2ETest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private CreatePartHandler createPartHandler;
    @Mock private FindPartByIdHandler findPartByIdHandler;
    @Mock private FindPartBySkuHandler findPartBySkuHandler;
    @Mock private ListPartsHandler listPartsHandler;
    @Mock private UpdatePartHandler updatePartHandler;
    @Mock private DeactivatePartHandler deactivatePartHandler;

    @BeforeEach
    void setUp() {
        PartController partController = new PartController(
                createPartHandler, findPartByIdHandler, findPartBySkuHandler,
                listPartsHandler, updatePartHandler, deactivatePartHandler);
        mockMvc = MockMvcBuilders.standaloneSetup(partController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Part createPart() {
        Part part = new Part();
        part.setId(1L);
        part.setName("Brake Pad");
        part.setSku("BP-001");
        part.setType(ProductType.PART);
        part.setUnit(UnitOfMeasure.UNIT);
        part.setCategory("Brakes");
        part.setBrand("Bosch");
        part.setCostPrice(new BigDecimal("45.00"));
        part.setSalePrice(new BigDecimal("90.00"));
        part.setActive(true);
        part.setManufacturerCode("MFG-001");
        part.setWarrantyMonths(12);
        part.setCreatedAt(LocalDateTime.now());
        part.setUpdatedAt(LocalDateTime.now());
        return part;
    }

    @Test
    void whenCreatingPartWithValidData_thenReturns201() throws Exception {
        Part part = createPart();
        when(createPartHandler.handle(any(CreatePartRequest.class))).thenReturn(part);

        CreatePartRequest request = new CreatePartRequest("Brake Pad", "BP-001", UnitOfMeasure.UNIT,
                "Brakes", "Bosch", new BigDecimal("45"), new BigDecimal("90"), "MFG-001", 12);

        mockMvc.perform(post("/api/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Brake Pad"))
                .andExpect(jsonPath("$.sku").value("BP-001"));
    }

    @Test
    void whenCreatingPartWithDuplicateSku_thenReturns400() throws Exception {
        when(createPartHandler.handle(any(CreatePartRequest.class)))
                .thenThrow(new IllegalArgumentException("A part with SKU 'BP-001' already exists"));

        CreatePartRequest request = new CreatePartRequest("Brake Pad", "BP-001", UnitOfMeasure.UNIT,
                "Brakes", "Bosch", new BigDecimal("45"), new BigDecimal("90"), "MFG-001", 12);

        mockMvc.perform(post("/api/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenFindingPartByExistingId_thenReturns200() throws Exception {
        Part part = createPart();
        when(findPartByIdHandler.handle(1L)).thenReturn(part);

        mockMvc.perform(get("/api/parts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("BP-001"));
    }

    @Test
    void whenFindingAllParts_thenReturns200WithList() throws Exception {
        when(listPartsHandler.handle()).thenReturn(List.of(createPart()));

        mockMvc.perform(get("/api/parts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void whenFindingPartByExistingSku_thenReturns200() throws Exception {
        Part part = createPart();
        when(findPartBySkuHandler.handle("BP-001")).thenReturn(part);

        mockMvc.perform(get("/api/parts/sku/{sku}", "BP-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Brake Pad"));
    }

    @Test
    void whenUpdatingPartWithValidData_thenReturns200() throws Exception {
        Part part = createPart();
        part.setName("Updated Pad");
        when(updatePartHandler.handle(eq(1L), any(UpdatePartRequest.class))).thenReturn(part);

        UpdatePartRequest request = new UpdatePartRequest("Updated Pad", "BP-001", UnitOfMeasure.UNIT,
                "Brakes", "Bosch", new BigDecimal("50"), new BigDecimal("100"), "MFG-002", 24);

        mockMvc.perform(put("/api/parts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Pad"));
    }

    @Test
    void whenDeactivatingPart_thenReturns204() throws Exception {
        doNothing().when(deactivatePartHandler).handle(1L);

        mockMvc.perform(delete("/api/parts/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}

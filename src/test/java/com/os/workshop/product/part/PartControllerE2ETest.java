package com.os.workshop.product.part;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.os.workshop.product.domain.Part;
import com.os.workshop.product.domain.ProductType;
import com.os.workshop.product.domain.UnitOfMeasure;
import com.os.workshop.product.exception.GlobalExceptionHandler;
import com.os.workshop.product.repository.PartRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PartControllerE2ETest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PartRepository partRepository;

    @BeforeEach
    void setUp() {
        PartService partService = new PartService(partRepository);
        PartController partController = new PartController(partService);
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
        when(partRepository.existsBySku("BP-001")).thenReturn(false);
        when(partRepository.save(any(Part.class))).thenAnswer(i -> {
            Part p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        PartRequest request = new PartRequest("Brake Pad", "BP-001", UnitOfMeasure.UNIT,
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
        when(partRepository.existsBySku("BP-001")).thenReturn(true);

        PartRequest request = new PartRequest("Brake Pad", "BP-001", UnitOfMeasure.UNIT,
                "Brakes", "Bosch", new BigDecimal("45"), new BigDecimal("90"), "MFG-001", 12);

        mockMvc.perform(post("/api/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenFindingPartByExistingId_thenReturns200() throws Exception {
        Part part = createPart();
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));

        mockMvc.perform(get("/api/parts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("BP-001"));
    }

    @Test
    void whenFindingAllParts_thenReturns200WithList() throws Exception {
        when(partRepository.findAllActive()).thenReturn(List.of(createPart()));

        mockMvc.perform(get("/api/parts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void whenFindingPartByExistingSku_thenReturns200() throws Exception {
        Part part = createPart();
        when(partRepository.findBySku("BP-001")).thenReturn(Optional.of(part));

        mockMvc.perform(get("/api/parts/sku/{sku}", "BP-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Brake Pad"));
    }

    @Test
    void whenUpdatingPartWithValidData_thenReturns200() throws Exception {
        Part part = createPart();
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));
        when(partRepository.save(any(Part.class))).thenAnswer(i -> i.getArgument(0));

        PartRequest request = new PartRequest("Updated Pad", "BP-001", UnitOfMeasure.UNIT,
                "Brakes", "Bosch", new BigDecimal("50"), new BigDecimal("100"), "MFG-002", 24);

        mockMvc.perform(put("/api/parts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Pad"));
    }

    @Test
    void whenDeactivatingPart_thenReturns204() throws Exception {
        Part part = createPart();
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));
        when(partRepository.save(any(Part.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(delete("/api/parts/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}

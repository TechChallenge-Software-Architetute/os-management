package com.os.workshop.features.service.adapter.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.domain.Status;
import com.os.workshop.features.service.domain.enums.ServiceStatusEnum;
import com.os.workshop.features.service.usecases.FindServiceByIdUC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FindServiceByIdControllerE2ETest {

    private MockMvc mockMvc;

    @Mock
    private FindServiceByIdUC findServiceByIdUC;

    @InjectMocks
    private FindServiceByIdController findServiceByIdController;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(findServiceByIdController)
                .setMessageConverters(converter)
                .build();
    }

    private ServiceEntity createServiceEntity() {
        UUID serviceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID osId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        return new ServiceEntity(
                serviceId,
                "TROCA_OLEO",
                osId,
                List.of(new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now()))
        );
    }

    // ==================== GET /services/{id} ====================

    @Test
    void whenFindingServiceByExistingId_thenReturns200() throws Exception {
        UUID serviceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        ServiceEntity entity = createServiceEntity();
        when(findServiceByIdUC.process(serviceId)).thenReturn(entity);

        mockMvc.perform(get("/services/{id}", serviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(serviceId.toString()))
                .andExpect(jsonPath("$.serviceTypeName").value("TROCA_OLEO"));
    }

    @Test
    void whenFindingServiceByNonExistingId_thenReturns404() throws Exception {
        UUID serviceId = UUID.randomUUID();
        when(findServiceByIdUC.process(serviceId))
                .thenThrow(new RuntimeException("Servico nao encontrado"));

        mockMvc.perform(get("/services/{id}", serviceId))
                .andExpect(status().isNotFound());
    }
}

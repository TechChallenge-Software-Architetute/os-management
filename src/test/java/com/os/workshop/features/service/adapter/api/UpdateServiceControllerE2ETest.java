package com.os.workshop.features.service.adapter.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.domain.Status;
import com.os.workshop.features.service.domain.enums.ServiceStatusEnum;
import com.os.workshop.features.service.domain.requests.UpdateServiceRequest;
import com.os.workshop.features.service.usecases.UpdateServiceUC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UpdateServiceControllerE2ETest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock
    private UpdateServiceUC updateServiceUC;

    @BeforeEach
    void setUp() {
        UpdateServiceController controller = new UpdateServiceController(updateServiceUC);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
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

    private UpdateServiceRequest createUpdateRequest() {
        var request = new UpdateServiceRequest();
        request.setServiceType("ALINHAMENTO");
        request.setIdOS(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        return request;
    }

    // ==================== PUT /services/{id} ====================

    @Test
    void whenUpdatingServiceWithValidData_thenReturns200() throws Exception {
        UUID serviceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        ServiceEntity updatedEntity = createServiceEntity();
        updatedEntity.setServiceTypeName("ALINHAMENTO");

        when(updateServiceUC.process(eq(serviceId), any(UpdateServiceRequest.class)))
                .thenReturn(updatedEntity);

        mockMvc.perform(put("/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUpdateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(serviceId.toString()))
                .andExpect(jsonPath("$.serviceTypeName").value("ALINHAMENTO"));
    }

    @Test
    void whenUpdatingServiceAndNotFound_thenReturns404() throws Exception {
        UUID serviceId = UUID.randomUUID();
        when(updateServiceUC.process(eq(serviceId), any(UpdateServiceRequest.class)))
                .thenThrow(new RuntimeException("Servico nao encontrado"));

        mockMvc.perform(put("/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUpdateRequest())))
                .andExpect(status().isNotFound());
    }
}

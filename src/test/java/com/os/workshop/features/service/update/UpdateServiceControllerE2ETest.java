package com.os.workshop.features.service.update;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.os.workshop.adapter.in.web.service.ServiceController;
import com.os.workshop.adapter.in.web.service.UpdateServiceRequest;
import com.os.workshop.application.service.*;
import com.os.workshop.domain.service.ServiceStatusEnum;
import com.os.workshop.domain.service.Status;
import com.os.workshop.domain.service.WorkshopService;
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
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock private CreateServiceUseCase createServiceUseCase;
    @Mock private FindServiceByIdUseCase findServiceByIdUseCase;
    @Mock private FindServicesByServiceOrderUseCase findServicesByServiceOrderUseCase;
    @Mock private ListServicesUseCase listServicesUseCase;
    @Mock private ListServiceTypesUseCase listServiceTypesUseCase;
    @Mock private UpdateServiceUseCase updateServiceUseCase;
    @Mock private UpdateServiceStatusUseCase updateServiceStatusUseCase;

    @BeforeEach
    void setUp() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        ServiceController controller = new ServiceController(
                createServiceUseCase, findServiceByIdUseCase, findServicesByServiceOrderUseCase,
                listServicesUseCase, listServiceTypesUseCase, updateServiceUseCase, updateServiceStatusUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(converter)
                .build();
    }

    private UpdateServiceRequest createUpdateRequest() {
        var request = new UpdateServiceRequest();
        request.setServiceType("ALINHAMENTO");
        request.setIdOS(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        return request;
    }

    @Test
    void whenUpdatingServiceWithValidData_thenReturns200() throws Exception {
        UUID serviceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID osId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        WorkshopService updatedService = new WorkshopService(serviceId, "ALINHAMENTO", osId,
                List.of(new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now())));

        when(updateServiceUseCase.execute(eq(serviceId), eq("ALINHAMENTO"), eq(osId)))
                .thenReturn(updatedService);

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
        when(updateServiceUseCase.execute(eq(serviceId), any(), any()))
                .thenThrow(new RuntimeException("Servico nao encontrado"));

        mockMvc.perform(put("/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUpdateRequest())))
                .andExpect(status().isNotFound());
    }
}

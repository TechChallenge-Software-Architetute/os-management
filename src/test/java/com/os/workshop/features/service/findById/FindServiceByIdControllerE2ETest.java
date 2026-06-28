package com.os.workshop.features.service.findById;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.os.workshop.adapter.in.web.service.ServiceController;
import com.os.workshop.application.service.*;
import com.os.workshop.domain.service.ServiceStatusEnum;
import com.os.workshop.domain.service.Status;
import com.os.workshop.domain.service.WorkshopService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Mock private CreateServiceUseCase createServiceUseCase;
    @Mock private FindServiceByIdUseCase findServiceByIdUseCase;
    @Mock private FindServicesByServiceOrderUseCase findServicesByServiceOrderUseCase;
    @Mock private ListServicesUseCase listServicesUseCase;
    @Mock private ListServiceTypesUseCase listServiceTypesUseCase;
    @Mock private UpdateServiceUseCase updateServiceUseCase;
    @Mock private UpdateServiceStatusUseCase updateServiceStatusUseCase;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        ServiceController controller = new ServiceController(
                createServiceUseCase, findServiceByIdUseCase, findServicesByServiceOrderUseCase,
                listServicesUseCase, listServiceTypesUseCase, updateServiceUseCase, updateServiceStatusUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(converter)
                .build();
    }

    private WorkshopService createWorkshopService() {
        UUID serviceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID osId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        return new WorkshopService(serviceId, "TROCA_OLEO", osId,
                List.of(new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now())));
    }

    @Test
    void whenFindingServiceByExistingId_thenReturns200() throws Exception {
        UUID serviceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(findServiceByIdUseCase.execute(serviceId)).thenReturn(createWorkshopService());

        mockMvc.perform(get("/services/{id}", serviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(serviceId.toString()))
                .andExpect(jsonPath("$.serviceTypeName").value("TROCA_OLEO"));
    }

    @Test
    void whenFindingServiceByNonExistingId_thenReturns404() throws Exception {
        UUID serviceId = UUID.randomUUID();
        when(findServiceByIdUseCase.execute(serviceId))
                .thenThrow(new RuntimeException("Servico nao encontrado"));

        mockMvc.perform(get("/services/{id}", serviceId))
                .andExpect(status().isNotFound());
    }
}

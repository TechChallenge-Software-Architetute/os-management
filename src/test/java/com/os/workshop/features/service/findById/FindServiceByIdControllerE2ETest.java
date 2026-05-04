package com.os.workshop.features.service.findById;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.os.workshop.features.service.ServiceController;
import com.os.workshop.features.service.create.CreateServiceHandler;
import com.os.workshop.features.service.findByServiceOrder.FindServicesByServiceOrderHandler;
import com.os.workshop.features.service.list.ListServicesHandler;
import com.os.workshop.features.service.listTypes.ListServiceTypesHandler;
import com.os.workshop.features.service.shared.domain.ServiceStatusEnum;
import com.os.workshop.features.service.shared.domain.Status;
import com.os.workshop.features.service.shared.repository.ServiceEntity;
import com.os.workshop.features.service.update.UpdateServiceHandler;
import com.os.workshop.features.service.updateStatus.UpdateServiceStatusHandler;
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

    @Mock private CreateServiceHandler createServiceHandler;
    @Mock private FindServiceByIdHandler findServiceByIdHandler;
    @Mock private FindServicesByServiceOrderHandler findServicesByServiceOrderHandler;
    @Mock private ListServicesHandler listServicesHandler;
    @Mock private ListServiceTypesHandler listServiceTypesHandler;
    @Mock private UpdateServiceHandler updateServiceHandler;
    @Mock private UpdateServiceStatusHandler updateServiceStatusHandler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        ServiceController controller = new ServiceController(
                createServiceHandler, findServiceByIdHandler, findServicesByServiceOrderHandler,
                listServicesHandler, listServiceTypesHandler, updateServiceHandler, updateServiceStatusHandler);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(converter)
                .build();
    }

    private ServiceEntity createServiceEntity() {
        UUID serviceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID osId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        return new ServiceEntity(
                serviceId, "TROCA_OLEO", osId,
                List.of(new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now()))
        );
    }

    @Test
    void whenFindingServiceByExistingId_thenReturns200() throws Exception {
        UUID serviceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        ServiceEntity entity = createServiceEntity();
        when(findServiceByIdHandler.handle(serviceId)).thenReturn(entity);

        mockMvc.perform(get("/services/{id}", serviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(serviceId.toString()))
                .andExpect(jsonPath("$.serviceTypeName").value("TROCA_OLEO"));
    }

    @Test
    void whenFindingServiceByNonExistingId_thenReturns404() throws Exception {
        UUID serviceId = UUID.randomUUID();
        when(findServiceByIdHandler.handle(serviceId))
                .thenThrow(new RuntimeException("Servico nao encontrado"));

        mockMvc.perform(get("/services/{id}", serviceId))
                .andExpect(status().isNotFound());
    }
}

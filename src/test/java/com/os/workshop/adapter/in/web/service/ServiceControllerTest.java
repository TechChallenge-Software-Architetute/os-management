package com.os.workshop.adapter.in.web.service;

import com.os.workshop.application.service.CreateServiceUseCase;
import com.os.workshop.application.service.FindServiceByIdUseCase;
import com.os.workshop.application.service.FindServicesByServiceOrderUseCase;
import com.os.workshop.application.service.ListServiceTypesUseCase;
import com.os.workshop.application.service.ListServicesUseCase;
import com.os.workshop.application.service.UpdateServiceStatusUseCase;
import com.os.workshop.application.service.UpdateServiceUseCase;
import com.os.workshop.domain.service.ServiceStatusEnum;
import com.os.workshop.domain.service.ServiceType;
import com.os.workshop.domain.service.Status;
import com.os.workshop.domain.service.WorkshopService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceControllerTest {

    @Mock private CreateServiceUseCase createServiceUseCase;
    @Mock private FindServiceByIdUseCase findServiceByIdUseCase;
    @Mock private FindServicesByServiceOrderUseCase findServicesByServiceOrderUseCase;
    @Mock private ListServicesUseCase listServicesUseCase;
    @Mock private ListServiceTypesUseCase listServiceTypesUseCase;
    @Mock private UpdateServiceUseCase updateServiceUseCase;
    @Mock private UpdateServiceStatusUseCase updateServiceStatusUseCase;

    @InjectMocks private ServiceController controller;

    private final UUID serviceId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();

    @Test
    void criaServico() {
        when(createServiceUseCase.execute("TROCA_OLEO", orderId)).thenReturn(service());
        var request = new CreateServiceRequest();
        request.setServiceType("TROCA_OLEO");
        request.setIdOS(orderId);

        var response = controller.createService(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().serviceTypeName()).isEqualTo("TROCA_OLEO");
        assertThat(response.getBody().idOS()).isEqualTo(orderId);
    }

    @Test
    void falhaAoCriarServicoRetorna500() {
        when(createServiceUseCase.execute(any(), any())).thenThrow(new RuntimeException("tipo inexistente"));

        var response = controller.createService(new CreateServiceRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void buscaPorId() {
        when(findServiceByIdUseCase.execute(serviceId)).thenReturn(service());

        var response = controller.findById(serviceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo(serviceId);
    }

    @Test
    void servicoInexistenteRetorna404() {
        when(findServiceByIdUseCase.execute(serviceId)).thenThrow(new RuntimeException("nao encontrado"));

        assertThat(controller.findById(serviceId).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void buscaPorOs() {
        when(findServicesByServiceOrderUseCase.execute(orderId)).thenReturn(List.of(service(), service()));

        var response = controller.findByIdOS(orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void falhaAoBuscarPorOsRetorna500() {
        when(findServicesByServiceOrderUseCase.execute(orderId)).thenThrow(new RuntimeException("falha"));

        assertThat(controller.findByIdOS(orderId).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void listaServicos() {
        when(listServicesUseCase.execute()).thenReturn(List.of(service()));

        var response = controller.listServices();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void falhaAoListarServicosRetorna500() {
        when(listServicesUseCase.execute()).thenThrow(new RuntimeException("falha"));

        assertThat(controller.listServices().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void listaTiposDeServico() {
        when(listServiceTypesUseCase.execute())
                .thenReturn(List.of(new ServiceType(UUID.randomUUID(), "TROCA_OLEO", "Troca de oleo")));

        var response = controller.listServiceTypes();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(ServiceTypeResponse::name).containsExactly("TROCA_OLEO");
    }

    @Test
    void falhaAoListarTiposRetorna500() {
        when(listServiceTypesUseCase.execute()).thenThrow(new RuntimeException("falha"));

        assertThat(controller.listServiceTypes().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void atualizaServico() {
        when(updateServiceUseCase.execute(serviceId, "ALINHAMENTO", orderId)).thenReturn(service());
        var request = new UpdateServiceRequest();
        request.setServiceType("ALINHAMENTO");
        request.setIdOS(orderId);

        assertThat(controller.update(serviceId, request).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void atualizarServicoInexistenteRetorna404() {
        when(updateServiceUseCase.execute(any(), any(), any())).thenThrow(new RuntimeException("nao encontrado"));

        assertThat(controller.update(serviceId, new UpdateServiceRequest()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void atualizaStatusDoServico() {
        when(updateServiceStatusUseCase.execute(serviceId, ServiceStatusEnum.DOING)).thenReturn(service());
        var request = new UpdateServiceStatusRequest();
        request.setId(serviceId);
        request.setStatus(ServiceStatusEnum.DOING);

        assertThat(controller.updateStatus(request).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void falhaAoAtualizarStatusRetorna500() {
        when(updateServiceStatusUseCase.execute(any(), any())).thenThrow(new RuntimeException("falha"));

        assertThat(controller.updateStatus(new UpdateServiceStatusRequest()).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private WorkshopService service() {
        return new WorkshopService(serviceId, "TROCA_OLEO", orderId,
                List.of(new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now())));
    }
}

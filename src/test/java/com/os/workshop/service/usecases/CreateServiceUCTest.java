package com.os.workshop.service.usecases;

import com.os.workshop.service.adapter.database.ServiceRepository;
import com.os.workshop.service.adapter.database.ServiceTypeRepository;
import com.os.workshop.service.domain.ServiceEntity;
import com.os.workshop.service.domain.ServiceTypeEntity;
import com.os.workshop.service.domain.enums.ServiceStatusEnum;
import com.os.workshop.service.domain.requests.CreateServiceRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateServiceUCTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    @InjectMocks
    private CreateServiceUC createServiceUC;

    private CreateServiceRequest createRequest(String serviceType, UUID idOS) {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setServiceType(serviceType);
        request.setIdOS(idOS);
        return request;
    }

    private ServiceTypeEntity createServiceType(String name) {
        ServiceTypeEntity type = new ServiceTypeEntity();
        type.setId(UUID.randomUUID());
        type.setName(name);
        type.setDescription("Descrição de " + name);
        return type;
    }

    @Test
    void whenCreatingServiceWithValidType_thenServiceIsSaved() {
        UUID idOS = UUID.randomUUID();
        String typeName = "TROCA_OLEO";
        CreateServiceRequest request = createRequest(typeName, idOS);
        ServiceTypeEntity serviceType = createServiceType(typeName);

        when(serviceTypeRepository.findByName(typeName)).thenReturn(Optional.of(serviceType));
        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(i -> {
            ServiceEntity entity = i.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ServiceEntity result = createServiceUC.process(request);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(typeName, result.getServiceTypeName());
        assertEquals(idOS, result.getIdOS());
        assertNotNull(result.getServiceStatus());
        assertEquals(1, result.getServiceStatus().size());
        assertEquals(ServiceStatusEnum.TO_DO, result.getServiceStatus().get(0).getStatus());
        verify(serviceRepository).save(any(ServiceEntity.class));
    }

    @Test
    void whenCreatingServiceWithInvalidType_thenThrowsRuntimeException() {
        UUID idOS = UUID.randomUUID();
        String typeName = "TIPO_INEXISTENTE";
        CreateServiceRequest request = createRequest(typeName, idOS);

        when(serviceTypeRepository.findByName(typeName)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> createServiceUC.process(request));

        assertEquals("Tipo de serviço não encontrado na base de Serviços.", exception.getMessage());
        verify(serviceRepository, never()).save(any(ServiceEntity.class));
    }

    @Test
    void whenCreatingService_thenInitialStatusIsToDo() {
        UUID idOS = UUID.randomUUID();
        String typeName = "ALINHAMENTO";
        CreateServiceRequest request = createRequest(typeName, idOS);
        ServiceTypeEntity serviceType = createServiceType(typeName);

        when(serviceTypeRepository.findByName(typeName)).thenReturn(Optional.of(serviceType));
        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(i -> i.getArgument(0));

        ServiceEntity result = createServiceUC.process(request);

        assertEquals(ServiceStatusEnum.TO_DO, result.getServiceStatus().get(0).getStatus());
        assertNotNull(result.getServiceStatus().get(0).getChangedAt());
    }

    @Test
    void whenCreatingService_thenServiceTypeNameMatchesFoundType() {
        UUID idOS = UUID.randomUUID();
        String typeName = "BALANCEAMENTO";
        CreateServiceRequest request = createRequest(typeName, idOS);
        ServiceTypeEntity serviceType = createServiceType(typeName);

        when(serviceTypeRepository.findByName(typeName)).thenReturn(Optional.of(serviceType));
        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(i -> i.getArgument(0));

        ServiceEntity result = createServiceUC.process(request);

        assertEquals(serviceType.getName(), result.getServiceTypeName());
    }
}

package com.os.workshop.features.service.create;

import com.os.workshop.application.service.CreateServiceUseCase;
import com.os.workshop.application.service.port.out.ServiceRepository;
import com.os.workshop.application.service.port.out.ServiceTypeRepository;
import com.os.workshop.domain.service.ServiceStatusEnum;
import com.os.workshop.domain.service.ServiceType;
import com.os.workshop.domain.service.WorkshopService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateServiceHandlerTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    @InjectMocks
    private CreateServiceUseCase createServiceUseCase;

    @Test
    void whenCreatingServiceWithValidType_thenServiceIsSaved() {
        UUID idOS = UUID.randomUUID();
        String typeName = "TROCA_OLEO";

        when(serviceTypeRepository.findByName(typeName)).thenReturn(Optional.of(new ServiceType(UUID.randomUUID(), typeName, "desc")));
        when(serviceRepository.save(any(WorkshopService.class))).thenAnswer(i -> {
            WorkshopService s = i.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        WorkshopService result = createServiceUseCase.execute(typeName, idOS);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(typeName, result.getServiceTypeName());
        assertEquals(idOS, result.getIdOS());
        assertEquals(1, result.getServiceStatus().size());
        assertEquals(ServiceStatusEnum.TO_DO, result.getServiceStatus().get(0).getStatus());
        verify(serviceRepository).save(any(WorkshopService.class));
    }

    @Test
    void whenCreatingServiceWithInvalidType_thenThrowsRuntimeException() {
        String typeName = "TIPO_INEXISTENTE";
        when(serviceTypeRepository.findByName(typeName)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> createServiceUseCase.execute(typeName, UUID.randomUUID()));

        assertEquals("Tipo de serviço não encontrado na base de Serviços.", exception.getMessage());
        verify(serviceRepository, never()).save(any(WorkshopService.class));
    }

    @Test
    void whenCreatingService_thenInitialStatusIsToDo() {
        UUID idOS = UUID.randomUUID();
        String typeName = "ALINHAMENTO";

        when(serviceTypeRepository.findByName(typeName)).thenReturn(Optional.of(new ServiceType(UUID.randomUUID(), typeName, "desc")));
        when(serviceRepository.save(any(WorkshopService.class))).thenAnswer(i -> i.getArgument(0));

        WorkshopService result = createServiceUseCase.execute(typeName, idOS);

        assertEquals(ServiceStatusEnum.TO_DO, result.getServiceStatus().get(0).getStatus());
        assertNotNull(result.getServiceStatus().get(0).getChangedAt());
    }

    @Test
    void whenCreatingService_thenServiceTypeNameMatchesFoundType() {
        UUID idOS = UUID.randomUUID();
        String typeName = "BALANCEAMENTO";

        when(serviceTypeRepository.findByName(typeName)).thenReturn(Optional.of(new ServiceType(UUID.randomUUID(), typeName, "desc")));
        when(serviceRepository.save(any(WorkshopService.class))).thenAnswer(i -> i.getArgument(0));

        WorkshopService result = createServiceUseCase.execute(typeName, idOS);

        assertEquals(typeName, result.getServiceTypeName());
    }
}

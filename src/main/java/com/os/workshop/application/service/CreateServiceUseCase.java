package com.os.workshop.application.service;

import com.os.workshop.application.service.port.out.ServiceRepository;
import com.os.workshop.application.service.port.out.ServiceTypeRepository;
import com.os.workshop.domain.service.ServiceStatusEnum;
import com.os.workshop.domain.service.Status;
import com.os.workshop.domain.service.WorkshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateServiceUseCase {

    private final ServiceRepository serviceRepository;
    private final ServiceTypeRepository serviceTypeRepository;

    public WorkshopService execute(String serviceType, UUID idOS) {
        var type = serviceTypeRepository.findByName(serviceType)
                .orElseThrow(() -> new RuntimeException("Tipo de serviço não encontrado na base de Serviços."));

        WorkshopService newService = new WorkshopService();
        newService.setServiceTypeName(type.getName());
        newService.setIdOS(idOS);
        newService.setServiceStatus(List.of(new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now())));

        return serviceRepository.save(newService);
    }
}

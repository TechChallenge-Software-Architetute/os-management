package com.os.workshop.application.service;

import com.os.workshop.application.service.port.out.ServiceRepository;
import com.os.workshop.application.service.port.out.ServiceTypeRepository;
import com.os.workshop.domain.service.WorkshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateServiceUseCase {

    private final ServiceRepository serviceRepository;
    private final ServiceTypeRepository serviceTypeRepository;

    public WorkshopService execute(UUID id, String serviceType, UUID idOS) {
        var service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servico nao encontrado. ID: " + id));

        var type = serviceTypeRepository.findByName(serviceType)
                .orElseThrow(() -> new RuntimeException("Tipo de servico nao encontrado: " + serviceType));

        service.setServiceTypeName(type.getName());
        service.setIdOS(idOS);

        return serviceRepository.save(service);
    }
}

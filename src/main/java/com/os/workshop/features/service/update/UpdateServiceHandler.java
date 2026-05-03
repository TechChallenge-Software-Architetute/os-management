package com.os.workshop.features.service.update;

import com.os.workshop.features.service.shared.repository.ServiceEntity;
import com.os.workshop.features.service.shared.repository.ServiceRepository;
import com.os.workshop.features.service.shared.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateServiceHandler {

    private final ServiceRepository serviceRepository;
    private final ServiceTypeRepository serviceTypeRepository;

    public ServiceEntity handle(UUID id, UpdateServiceRequest request) {
        var service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servico nao encontrado. ID: " + id));

        var serviceType = serviceTypeRepository.findByName(request.getServiceType())
                .orElseThrow(() -> new RuntimeException("Tipo de servico nao encontrado: " + request.getServiceType()));

        service.setServiceTypeName(serviceType.getName());
        service.setIdOS(request.getIdOS());

        return serviceRepository.save(service);
    }
}

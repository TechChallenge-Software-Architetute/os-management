package com.os.workshop.service.usecases;

import com.os.workshop.service.adapter.database.ServiceRepository;
import com.os.workshop.service.adapter.database.ServiceTypeRepository;
import com.os.workshop.service.domain.ServiceEntity;
import com.os.workshop.service.domain.requests.UpdateServiceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateServiceUC {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    public ServiceEntity process(UUID id, UpdateServiceRequest request) {
        var service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servico nao encontrado. ID: " + id));

        var serviceType = serviceTypeRepository.findByName(request.getServiceType())
                .orElseThrow(() -> new RuntimeException("Tipo de servico nao encontrado: " + request.getServiceType()));

        service.setServiceTypeName(serviceType.getName());
        service.setIdOS(request.getIdOS());

        return serviceRepository.save(service);
    }
}

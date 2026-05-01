package com.os.workshop.service.usecases;

import com.os.workshop.service.adapter.database.ServiceRepository;
import com.os.workshop.service.domain.ServiceEntity;
import com.os.workshop.service.domain.Status;
import com.os.workshop.service.domain.requests.UpdateStatusServiceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UpdateServiceStatusUC {

    @Autowired
    private ServiceRepository serviceRepository;

    public ServiceEntity process(UpdateStatusServiceRequest request) {

        var service = serviceRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado. ID: " + request.getId()));

        var statusList = service.getServiceStatus();

        statusList.add(new Status(request.getStatus(), LocalDateTime.now()));

        service.setServiceStatus(statusList);

        serviceRepository.save(service);

        return service;
    }

}

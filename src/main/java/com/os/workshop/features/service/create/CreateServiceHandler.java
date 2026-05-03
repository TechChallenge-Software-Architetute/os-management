package com.os.workshop.features.service.create;

import com.os.workshop.features.service.shared.repository.ServiceEntity;
import com.os.workshop.features.service.shared.domain.ServiceStatusEnum;
import com.os.workshop.features.service.shared.domain.Status;
import com.os.workshop.features.service.shared.repository.ServiceRepository;
import com.os.workshop.features.service.shared.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateServiceHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateServiceHandler.class);

    private final ServiceRepository serviceRepository;
    private final ServiceTypeRepository serviceTypeRepository;

    public ServiceEntity handle(CreateServiceRequest request) {
        logger.info("Iniciando criação de serviço. Tipo: {}", request.getServiceType());

        var serviceType = serviceTypeRepository.findByName(request.getServiceType())
                .orElseThrow(() -> {
                    logger.error("Tipo de serviço não encontrado: {}", request.getServiceType());
                    return new RuntimeException("Tipo de serviço não encontrado na base de Serviços.");
                });

        ServiceEntity newService = new ServiceEntity();
        newService.setServiceTypeName(serviceType.getName());
        newService.setIdOS(request.getIdOS());
        newService.setServiceStatus(List.of(new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now())));

        ServiceEntity createdService = serviceRepository.save(newService);
        logger.info("Serviço criado com sucesso. ID: {}, Tipo: {}", createdService.getId(), createdService.getServiceTypeName());

        return createdService;
    }
}

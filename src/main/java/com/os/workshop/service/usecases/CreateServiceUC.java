package com.os.workshop.service.usecases;

import com.os.workshop.service.domain.ServiceEntity;
import com.os.workshop.service.domain.requests.CreateServiceRequest;
import com.os.workshop.service.adapter.database.ServiceRepository;
import com.os.workshop.service.adapter.database.ServiceTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateServiceUC {

    private static final Logger logger = LoggerFactory.getLogger(CreateServiceUC.class);

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    public ServiceEntity process(CreateServiceRequest request) {
        logger.info("Iniciando criação de serviço. Tipo: {}", request.getServiceType());

        // Valida se o tipo de serviço existe
        var serviceType = serviceTypeRepository.findByName(request.getServiceType())
                .orElseThrow(() -> {
                    logger.error("Tipo de serviço não encontrado: {}", request.getServiceType());
                    return new RuntimeException("Tipo de serviço não encontrado na base de Serviços.");
                });

        // Cria nova entidade de serviço
        ServiceEntity newService = new ServiceEntity();
        newService.setServiceTypeName(serviceType.getName());
        newService.setIdOS(request.getIdOS());

        // Salva no repositório
        ServiceEntity createdService = serviceRepository.save(newService);
        logger.info("Serviço criado com sucesso. ID: {}, Tipo: {}", createdService.getId(), createdService.getServiceTypeName());

        return createdService;
    }
}

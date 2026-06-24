package com.os.workshop.features.service.list;

import com.os.workshop.features.service.shared.repository.ServiceEntity;
import com.os.workshop.features.service.shared.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListServicesHandler {

    private static final Logger logger = LoggerFactory.getLogger(ListServicesHandler.class);

    private final ServiceRepository serviceRepository;

    public List<ServiceEntity> handle() {
        logger.info("Iniciando listagem de serviços");

        try {
            List<ServiceEntity> allServices = serviceRepository.findAll();
            logger.info("Listagem de serviços concluída. Total de serviços: {}", allServices.size());
            return allServices;
        } catch (Exception e) {
            logger.error("Erro ao listar serviços: {}", e.getMessage(), e);
            throw e;
        }
    }
}

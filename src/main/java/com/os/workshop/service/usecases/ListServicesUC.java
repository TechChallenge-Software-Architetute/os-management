package com.os.workshop.service.usecases;

import com.os.workshop.service.domain.ServiceEntity;
import com.os.workshop.service.adapter.database.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListServicesUC {

    private static final Logger logger = LoggerFactory.getLogger(ListServicesUC.class);

    @Autowired
    private ServiceRepository serviceRepository;

    public List<ServiceEntity> process() {
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

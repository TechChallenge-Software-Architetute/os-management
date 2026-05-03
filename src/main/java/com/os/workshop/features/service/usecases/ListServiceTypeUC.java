package com.os.workshop.features.service.usecases;

import com.os.workshop.features.service.domain.ServiceTypeEntity;
import com.os.workshop.features.service.adapter.database.ServiceTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListServiceTypeUC {

    private static final Logger logger = LoggerFactory.getLogger(ListServiceTypeUC.class);

    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    public List<ServiceTypeEntity> process() {
        logger.info("Iniciando listagem de serviços");

        try {
            return serviceTypeRepository.findAll();
        } catch (Exception e) {
            logger.error("Erro ao listar serviços: {}", e.getMessage(), e);
            throw e;
        }
    }
}

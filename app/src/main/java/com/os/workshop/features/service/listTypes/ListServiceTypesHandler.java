package com.os.workshop.features.service.listTypes;

import com.os.workshop.features.service.shared.repository.ServiceTypeEntity;
import com.os.workshop.features.service.shared.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListServiceTypesHandler {

    private static final Logger logger = LoggerFactory.getLogger(ListServiceTypesHandler.class);

    private final ServiceTypeRepository serviceTypeRepository;

    public List<ServiceTypeEntity> handle() {
        logger.info("Iniciando listagem de serviços");

        try {
            return serviceTypeRepository.findAll();
        } catch (Exception e) {
            logger.error("Erro ao listar serviços: {}", e.getMessage(), e);
            throw e;
        }
    }
}

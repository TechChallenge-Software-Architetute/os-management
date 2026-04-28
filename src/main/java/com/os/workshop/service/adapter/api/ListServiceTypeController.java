package com.os.workshop.service.adapter.api;

import com.os.workshop.service.domain.ServiceTypeEntity;
import com.os.workshop.service.usecases.ListServiceTypeUC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/service-types")
public class ListServiceTypeController {

    private static final Logger logger = LoggerFactory.getLogger(ListServiceTypeController.class);

    @Autowired
    private ListServiceTypeUC listServiceTypeUC;

    @GetMapping
    public ResponseEntity<List<ServiceTypeEntity>> listServices(
            @RequestHeader(value = "correlationId") String correlationId
    ) {
        logger.info("Recebida requisição para listar serviços. CorrelationId: {}", correlationId);
        try {
            List<ServiceTypeEntity> serviceType = listServiceTypeUC.process();
            logger.info("Tipos de Serviços listados com sucesso. Tipos: {}", serviceType);
            return ResponseEntity.ok(serviceType);
        } catch (Exception e) {
            logger.error("Erro ao listar serviços. CorrelationId: {}, Erro: {}", correlationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
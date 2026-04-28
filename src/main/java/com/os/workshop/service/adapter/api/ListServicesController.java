package com.os.workshop.service.adapter.api;

import com.os.workshop.service.domain.ServiceEntity;
import com.os.workshop.service.usecases.ListServicesUC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/services")
public class ListServicesController {

    private static final Logger logger = LoggerFactory.getLogger(ListServicesController.class);

    @Autowired
    private ListServicesUC listServicesUC;

    @GetMapping
    public ResponseEntity<List<ServiceEntity>> listServices() {
        logger.info("Recebida requisição para listar serviços.");
        try {
            List<ServiceEntity> services = listServicesUC.process();
            logger.info("Serviços listados com sucesso. Serviços: {}", services);
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            logger.error("Erro ao listar serviços. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
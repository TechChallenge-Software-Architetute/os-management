package com.os.workshop.service.adapter.api;

import com.os.workshop.service.domain.ServiceEntity;
import com.os.workshop.service.usecases.FindServicesByIdOSUC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/services")
public class FindServicesByIdOSController {

    private static final Logger logger = LoggerFactory.getLogger(FindServicesByIdOSController.class);

    @Autowired
    private FindServicesByIdOSUC findServicesByIdOSUC;

    @GetMapping("/os/{idOS}")
    public ResponseEntity<List<ServiceEntity>> findByIdOS(@PathVariable UUID idOS) {
        logger.info("Recebida requisicao para buscar servicos por ID OS: {}", idOS);

        try {
            var services = findServicesByIdOSUC.process(idOS);

            return ResponseEntity.ok(services);
        } catch (Exception e) {
            logger.error("Erro ao buscar servicos por ID OS. ID OS: {}. Erro: {}", idOS, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

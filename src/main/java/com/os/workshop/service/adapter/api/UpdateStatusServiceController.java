package com.os.workshop.service.adapter.api;

import com.os.workshop.service.domain.ServiceEntity;
import com.os.workshop.service.domain.requests.UpdateStatusServiceRequest;
import com.os.workshop.service.usecases.UpdateServiceStatusUC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/services")
public class UpdateStatusServiceController {

    private static final Logger logger = LoggerFactory.getLogger(UpdateStatusServiceController.class);

    @Autowired
    private UpdateServiceStatusUC updateServiceStatusUC;

    @PatchMapping("/update-status")
    public ResponseEntity<ServiceEntity> listServices(
            @RequestBody UpdateStatusServiceRequest request
    ) {
        logger.info("Recebida requisição para atualizar status serviços.");
        try {
            var service = updateServiceStatusUC.process(request);
            logger.info("Serviço atualizado. Serviço: {}", service);
            return ResponseEntity.ok().body(service);
        } catch (Exception e) {
            logger.error("Erro ao listar serviços. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

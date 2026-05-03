package com.os.workshop.features.service.adapter.api;

import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.domain.requests.UpdateServiceRequest;
import com.os.workshop.features.service.usecases.UpdateServiceUC;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/services")
@AllArgsConstructor
public class UpdateServiceController {

    private static final Logger logger = LoggerFactory.getLogger(UpdateServiceController.class);

    private UpdateServiceUC updateServiceUC;

    @PutMapping("/{id}")
    public ResponseEntity<ServiceEntity> update(
            @PathVariable UUID id,
            @RequestBody UpdateServiceRequest request
    ) {
        logger.info("Recebida requisicao para atualizar servico. ID: {}", id);

        try {
            var service = updateServiceUC.process(id, request);

            logger.info("Servico atualizado. Servico: {}", service);

            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            logger.error("Erro ao atualizar servico. ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar servico. ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

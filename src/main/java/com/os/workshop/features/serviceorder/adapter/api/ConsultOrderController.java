package com.os.workshop.features.serviceorder.adapter.api;

import com.os.workshop.features.serviceorder.domain.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.usecases.ConsultOrderUC;
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
@RequestMapping("/order")
public class ConsultOrderController {

    private static final Logger logger = LoggerFactory.getLogger(ConsultOrderController.class);

    @Autowired
    private ConsultOrderUC consultOrderUC;

    @GetMapping
    public ResponseEntity<List<ServiceOrderEntity>> consultOrders() {
        logger.info("Recebida requisicao para consultar ordens de servico.");

        try {
            var orders = consultOrderUC.process();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Erro ao consultar ordens de servico. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOrderEntity> consultOrderById(@PathVariable UUID id) {
        logger.info("Recebida requisicao para consultar ordem de servico por ID: {}", id);

        try {
            var order = consultOrderUC.process(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            logger.error("Ordem de servico nao encontrada. ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Erro ao consultar ordem de servico por ID. ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

package com.os.workshop.features.serviceorder.findById;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderHandler;
import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderResponse;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;
import com.os.workshop.features.serviceorder.shared.mapper.ServiceOrderMapper;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FindOrderByIdHandler {

    private static final Logger logger = LoggerFactory.getLogger(FindOrderByIdHandler.class);

    @Autowired
    private ServiceOrderJpaRepository serviceOrderJpaRepository;

    @Autowired
    private FindBudgetByServiceOrderHandler findBudgetByServiceOrderHandler;

    public ServiceOrder handle(UUID id) {
        logger.info("Consultando ordem de servico por ID: {}", id);

        var entity = serviceOrderJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de servico nao encontrada. ID: " + id));

        var budget = findBudgetByServiceOrderHandler.handle(entity.getId())
                .map(FindBudgetByServiceOrderResponse::from)
                .orElse(null);

        return ServiceOrderMapper.toDomain(entity, budget);
    }
}

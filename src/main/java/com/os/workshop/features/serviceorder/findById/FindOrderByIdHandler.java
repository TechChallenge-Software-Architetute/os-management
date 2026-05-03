package com.os.workshop.features.serviceorder.findById;

import com.os.workshop.features.budget.BudgetResponse;
import com.os.workshop.features.budget.BudgetService;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import com.os.workshop.features.serviceorder.shared.mapper.ServiceOrderMapper;
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
    private BudgetService budgetService;

    public ServiceOrder handle(UUID id) {
        logger.info("Consultando ordem de servico por ID: {}", id);

        var entity = serviceOrderJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de servico nao encontrada. ID: " + id));

        var budget = budgetService.findByServiceOrderId(entity.getId())
                .map(BudgetResponse::from)
                .orElse(null);

        return ServiceOrderMapper.toDomain(entity, budget);
    }
}

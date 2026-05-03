package com.os.workshop.features.serviceorder.update;

import com.os.workshop.features.budget.BudgetResponse;
import com.os.workshop.features.budget.BudgetService;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;
import com.os.workshop.features.serviceorder.shared.mapper.ServiceOrderMapper;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class UpdateOrderHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpdateOrderHandler.class);

    @Autowired
    private ServiceOrderJpaRepository serviceOrderJpaRepository;

    @Autowired
    private BudgetService budgetService;

    public ServiceOrder handle(UUID id, UpdateOrderRequest request) {
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("Status da ordem de servico e obrigatorio.");
        }

        var order = serviceOrderJpaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ordem de servico nao encontrada com id: " + id));

        order.setServiceStatus(request.getStatus().getStatus());

        var updatedOrder = serviceOrderJpaRepository.save(order);

        logger.info("Ordem de servico atualizada para o status {}.", updatedOrder.getServiceStatus());

        var budget = budgetService.findByServiceOrderId(updatedOrder.getId())
                .map(BudgetResponse::from)
                .orElse(null);

        return ServiceOrderMapper.toDomain(updatedOrder, budget);
    }
}

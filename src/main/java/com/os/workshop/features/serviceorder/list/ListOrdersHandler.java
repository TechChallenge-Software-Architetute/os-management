package com.os.workshop.features.serviceorder.list;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderHandler;
import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderResponse;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import com.os.workshop.features.serviceorder.shared.mapper.ServiceOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListOrdersHandler {

    private static final Logger logger = LoggerFactory.getLogger(ListOrdersHandler.class);

    @Autowired
    private ServiceOrderJpaRepository serviceOrderJpaRepository;

    @Autowired
    private FindBudgetByServiceOrderHandler findBudgetByServiceOrderHandler;

    public List<ServiceOrder> handle() {
        logger.info("Consultando todas as ordens de servico.");

        return serviceOrderJpaRepository.findAll().stream()
                .map(entity -> {
                    var budget = findBudgetByServiceOrderHandler.handle(entity.getId())
                            .map(FindBudgetByServiceOrderResponse::from)
                            .orElse(null);
                    return ServiceOrderMapper.toDomain(entity, budget);
                })
                .toList();
    }
}

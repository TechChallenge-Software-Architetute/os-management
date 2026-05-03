package com.os.workshop.features.serviceorder.usecases;

import com.os.workshop.features.serviceorder.adapter.database.OrderRepository;
import com.os.workshop.features.serviceorder.domain.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.domain.UpdateOrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class UpdateOrderUC {

    private static final Logger logger = LoggerFactory.getLogger(UpdateOrderUC.class);

    @Autowired
    private OrderRepository orderRepository;

    public ServiceOrderEntity process(UUID id, UpdateOrderRequest request) {
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("Status da ordem de servico e obrigatorio.");
        }

        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ordem de servico nao encontrada com id: " + id));

        order.setServiceStatus(request.getStatus().getStatus());

        var updatedOrder = orderRepository.save(order);

        logger.info("Ordem de servico atualizada para o status {}.", updatedOrder.getServiceStatus());

        return updatedOrder;
    }
}

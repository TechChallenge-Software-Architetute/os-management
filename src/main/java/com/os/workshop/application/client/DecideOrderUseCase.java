package com.os.workshop.application.client;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.domain.serviceorder.Decision;
import com.os.workshop.domain.serviceorder.OrderRejectedEvent;
import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DecideOrderUseCase {

    private final ClientRepository clientRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void execute(String email, UUID orderId, Decision decision, String reason) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFoundException("email: " + email));

        ServiceOrder order = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + orderId));

        if (!order.getCpfCnpj().equals(client.getDocument().getValue())) {
            throw new SecurityException("Order " + orderId + " does not belong to this client");
        }

        switch (decision) {
            case APPROVED -> order.advanceTo(OrderServiceStatusEnum.APROVADO);
            case REJECTED -> order.reject(reason);
        }

        serviceOrderRepository.save(order);

        if (decision == Decision.REJECTED) {
            eventPublisher.publishEvent(new OrderRejectedEvent(orderId));
        }
    }
}

package com.os.workshop.application.client;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderHandler;
import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderResponse;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindMyOrderDetailUseCase {

    private final ClientRepository clientRepository;
    private final ServiceOrderJpaRepository serviceOrderJpaRepository;
    private final FindBudgetByServiceOrderHandler findBudgetByServiceOrderHandler;

    @Transactional(readOnly = true)
    public FindMyOrderDetailResult execute(String email, UUID orderId) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFoundException("email: " + email));

        ServiceOrderEntity order = serviceOrderJpaRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        if (!order.getCpfCnpj().equals(client.getCpf().getValue())) {
            throw new IllegalArgumentException("Order " + orderId + " does not belong to this client");
        }

        FindBudgetByServiceOrderResponse budget = findBudgetByServiceOrderHandler.handle(orderId)
                .map(FindBudgetByServiceOrderResponse::from)
                .orElse(null);

        return new FindMyOrderDetailResult(order, budget);
    }

    public record FindMyOrderDetailResult(
            ServiceOrderEntity order,
            FindBudgetByServiceOrderResponse budget
    ) {}
}

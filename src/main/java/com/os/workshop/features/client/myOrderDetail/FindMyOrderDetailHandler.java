package com.os.workshop.features.client.myOrderDetail;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderHandler;
import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderResponse;
import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.client.shared.exception.ClientNotFoundException;
import com.os.workshop.features.client.shared.repository.ClientRepository;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Returns detailed information about a single service order for the logged-in client.
 * Includes the budget with item-level price breakdown when available.
 * Validates that the order belongs to the authenticated client.
 */
@Service
@RequiredArgsConstructor
public class FindMyOrderDetailHandler {

    private final ClientRepository clientRepository;
    private final ServiceOrderJpaRepository serviceOrderJpaRepository;
    private final FindBudgetByServiceOrderHandler findBudgetByServiceOrderHandler;

    @Transactional(readOnly = true)
    public FindMyOrderDetailResult handle(String email, UUID orderId) {
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

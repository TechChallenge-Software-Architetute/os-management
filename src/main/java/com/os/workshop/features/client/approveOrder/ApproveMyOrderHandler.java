package com.os.workshop.features.client.approveOrder;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderHandler;
import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderResponse;
import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.client.shared.exception.ClientNotFoundException;
import com.os.workshop.features.client.shared.repository.ClientRepository;
import com.os.workshop.features.serviceorder.shared.domain.enums.OrderServiceStatusEnum;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Approves a service order on behalf of the logged-in client.
 * The order must be in AGUARDANDO_APROVACAO status and belong to the client.
 */
@Service
@RequiredArgsConstructor
public class ApproveMyOrderHandler {

    private final ClientRepository clientRepository;
    private final ServiceOrderJpaRepository serviceOrderJpaRepository;
    private final FindBudgetByServiceOrderHandler findBudgetByServiceOrderHandler;

    @Transactional
    public ApproveMyOrderResult handle(String email, UUID orderId) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFoundException("email: " + email));

        ServiceOrderEntity order = serviceOrderJpaRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        if (!order.getCpfCnpj().equals(client.getCpf().getValue())) {
            throw new IllegalArgumentException("Order " + orderId + " does not belong to this client");
        }

        if (!OrderServiceStatusEnum.AGUARDANDO_APROVACAO.getStatus().equals(order.getServiceStatus())) {
            throw new IllegalStateException(
                    "Order " + orderId + " cannot be approved. Current status: " + order.getServiceStatus()
                            + ". Expected: " + OrderServiceStatusEnum.AGUARDANDO_APROVACAO.getStatus());
        }

        order.setServiceStatus(OrderServiceStatusEnum.APROVADO.getStatus());
        ServiceOrderEntity savedOrder = serviceOrderJpaRepository.save(order);

        FindBudgetByServiceOrderResponse budget = findBudgetByServiceOrderHandler.handle(orderId)
                .map(FindBudgetByServiceOrderResponse::from)
                .orElse(null);

        return new ApproveMyOrderResult(savedOrder, budget);
    }

    public record ApproveMyOrderResult(
            ServiceOrderEntity order,
            FindBudgetByServiceOrderResponse budget
    ) {}
}

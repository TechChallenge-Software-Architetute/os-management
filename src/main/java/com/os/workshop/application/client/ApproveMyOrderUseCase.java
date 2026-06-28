package com.os.workshop.application.client;

import com.os.workshop.application.budget.FindBudgetByServiceOrderUseCase;
import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.domain.budget.Budget;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApproveMyOrderUseCase {

    private final ClientRepository clientRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final FindBudgetByServiceOrderUseCase findBudgetByServiceOrderUseCase;

    @Transactional
    public ApproveMyOrderResult execute(String email, UUID orderId) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFoundException("email: " + email));

        ServiceOrder order = serviceOrderRepository.findById(orderId)
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
        ServiceOrder savedOrder = serviceOrderRepository.save(order);

        Budget budget = findBudgetByServiceOrderUseCase.execute(orderId).orElse(null);
        return new ApproveMyOrderResult(savedOrder, budget);
    }

    public record ApproveMyOrderResult(ServiceOrder order, Budget budget) {}
}

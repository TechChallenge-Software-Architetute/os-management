package com.os.workshop.application.client;

import com.os.workshop.application.budget.FindBudgetByServiceOrderUseCase;
import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.domain.budget.Budget;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindMyOrderDetailUseCase {

    private final ClientRepository clientRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final FindBudgetByServiceOrderUseCase findBudgetByServiceOrderUseCase;

    @Transactional(readOnly = true)
    public FindMyOrderDetailResult execute(String clientIdentifier, UUID orderId) {
        Client client = ClientLookup.resolve(clientRepository, clientIdentifier);

        ServiceOrder order = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        if (!order.getCpfCnpj().equals(client.getDocument().getValue())) {
            throw new IllegalArgumentException("Order " + orderId + " does not belong to this client");
        }

        Budget budget = findBudgetByServiceOrderUseCase.execute(orderId).orElse(null);
        return new FindMyOrderDetailResult(order, budget);
    }

    public record FindMyOrderDetailResult(ServiceOrder order, Budget budget) {}
}

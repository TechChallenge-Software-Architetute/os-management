package com.os.workshop.application.serviceorder;

import com.os.workshop.application.client.FindClientByCpfUseCase;
import com.os.workshop.application.notification.OrderStatusNotificationService;
import com.os.workshop.application.service.CreateServiceUseCase;
import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.application.vehicle.FindVehicleByPlateUseCase;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import com.os.workshop.infrastructure.monitoring.ServiceOrderMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

    private final CreateServiceUseCase createServiceUseCase;
    private final FindClientByCpfUseCase findClientByCpfUseCase;
    private final FindVehicleByPlateUseCase findVehicleByPlateUseCase;
    private final ServiceOrderRepository serviceOrderRepository;
    private final OrderStatusNotificationService notificationService;
    private final ServiceOrderMetrics metrics;

    public ServiceOrder execute(String cpfCnpj, String placaVeiculo, List<String> serviceTypes) {
        try {
            findClientByCpfUseCase.execute(cpfCnpj);
            findVehicleByPlateUseCase.execute(placaVeiculo);

            ServiceOrder order = ServiceOrder.create(cpfCnpj, placaVeiculo, serviceTypes);

            serviceTypes.forEach(service -> createServiceUseCase.execute(service, order.getId()));

            ServiceOrder saved = serviceOrderRepository.save(order);

            notificationService.notifyStatusChange(saved);
            metrics.created();

            return saved;
        } catch (RuntimeException exception) {
            metrics.processingFailed("create");
            throw exception;
        }
    }
}
